package com.zergatul.scripting.generator;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.type.SBoolean;
import com.zergatul.scripting.visitors.AwaitVisitor;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.symbols.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BinderTreeGenerator {

    public final List<StateBoundary> boundaries = new ArrayList<>();

    private StateBoundary currentBoundary;

    public void generate(List<BoundStatementNode> statements) {
        ReturnTransformer transformer = new ReturnTransformer();
        statements = statements.stream().map(transformer::process).toList();

        currentBoundary = newBoundary();
        for (BoundStatementNode statement : statements) {
            rewriteStatement(statement);
        }

        boolean isLastReturn = statements.get(statements.size() - 1).getNodeType() == NodeType.RETURN_STATEMENT;
        if (!isLastReturn) {
            add(new BoundGeneratorReturnNode());
        }
    }

    private void rewriteStatement(BoundStatementNode node) {
        if (isAsync(node)) {
            switch (node.getNodeType()) {
                case AUGMENTED_ASSIGNMENT_STATEMENT -> rewrite((BoundAugmentedAssignmentStatementNode) node);
                case BLOCK_STATEMENT -> rewrite((BoundBlockStatementNode) node);
                case EXPRESSION_STATEMENT -> rewrite((BoundExpressionStatementNode) node);
                case FOR_LOOP_STATEMENT -> rewrite((BoundForLoopStatementNode) node);
                case IF_STATEMENT -> rewrite((BoundIfStatementNode) node);
                case VARIABLE_DECLARATION -> rewrite((BoundVariableDeclarationNode) node);
                default -> throw new InternalException(String.format("Async %s not supported yet.", node.getNodeType()));
            }
        } else {
            markVariableDeclarations(node);
            liftCrossBoundaryVariables(node);
            add(node);
        }
    }

    private void rewrite(BoundBlockStatementNode node) {
        for (BoundStatementNode statement : node.statements) {
            rewriteStatement(statement);
        }
    }

    private void rewrite(BoundExpressionStatementNode node) {
        BoundExpressionNode expression = rewriteExpression(node.expression);
        add(new BoundExpressionStatementNode(expression, null));
    }

    private void rewrite(BoundIfStatementNode node) {
        LiftedVariable condition = new LiftedVariable(new LocalVariable(null, SBoolean.instance, null));
        storeExpressionValue(condition, node.condition);

        StateBoundary original = currentBoundary;
        StateBoundary thenTempBoundary = new StateBoundary();
        StateBoundary elseTempBoundary = new StateBoundary();
        StateBoundary end = new StateBoundary();

        currentBoundary = thenTempBoundary;
        rewriteStatement(node.thenStatement);
        add(new BoundSetGeneratorStateNode(end));
        BoundBlockStatementNode thenBlock = new BoundBlockStatementNode(thenTempBoundary.statements);

        BoundBlockStatementNode elseBlock;
        if (node.elseStatement != null) {
            currentBoundary = elseTempBoundary;
            rewriteStatement(node.elseStatement);
            add(new BoundSetGeneratorStateNode(end));
            elseBlock = new BoundBlockStatementNode(elseTempBoundary.statements);
        } else {
            elseBlock = new BoundBlockStatementNode(List.of(new BoundSetGeneratorStateNode(end)));
        }

        original.statements.add(new BoundIfStatementNode(
                new BoundNameExpressionNode(condition, null),
                thenBlock,
                elseBlock,
                TextRange.EMPTY));

        currentBoundary = end;
        end.index = boundaries.size();
        boundaries.add(end);
    }

    private void rewrite(BoundVariableDeclarationNode node) {
        assert node.expression != null;
        assert isAsync(node.expression);

        BoundExpressionNode expression = rewriteExpression(node.expression);
        add(new BoundVariableDeclarationNode(
                node.type,
                node.name,
                expression,
                node.getRange()));
    }

    private void rewrite(BoundForLoopStatementNode node) {
        rewriteStatement(node.init);

        StateBoundary begin = new StateBoundary();
        StateBoundary cont = new StateBoundary();
        StateBoundary end = new StateBoundary();
        add(new BoundSetGeneratorStateNode(begin));

        makeCurrent(begin);

        if (node.condition != null) {
            BoundExpressionNode expression = rewriteExpression(node.condition);
            expression = new BoundUnaryExpressionNode(
                    new BoundUnaryOperatorNode(SBoolean.instance.not(), null),
                    expression,
                    null);
            add(new BoundIfStatementNode(
                    expression,
                    new BoundBlockStatementNode(new BoundSetGeneratorStateNode(end), new BoundGeneratorContinueNode()),
                    null,
                    null));
        }

        LoopBodyTransformer transformer = new LoopBodyTransformer(node.body, cont, end);
        rewriteStatement(transformer.process());
        add(new BoundSetGeneratorStateNode(cont));

        makeCurrent(cont);
        rewriteStatement(node.update);

        add(new BoundSetGeneratorStateNode(begin));

        makeCurrent(end);
    }

    private void rewrite(BoundAugmentedAssignmentStatementNode node) {
        if (isAsync(node.left)) {
            throw new InternalException("Async left side augmented assignment is not supported yet.");
        }

        BoundExpressionNode expression = rewriteExpression(node.right);
        add(new BoundAugmentedAssignmentStatementNode(
                node.left,
                node.assignmentOperator,
                node.operator,
                expression,
                null));
    }

    private BoundExpressionNode rewriteExpression(BoundExpressionNode node) {
        if (isAsync(node)) {
            return switch (node.getNodeType()) {
                case AWAIT_EXPRESSION -> rewrite((BoundAwaitExpressionNode) node);
                case BINARY_EXPRESSION -> rewrite((BoundBinaryExpressionNode) node);
                case METHOD_INVOCATION_EXPRESSION -> rewrite((BoundMethodInvocationExpressionNode) node);
                case UNARY_EXPRESSION -> rewrite((BoundUnaryExpressionNode) node);
                default ->
                        throw new InternalException(String.format("Async %s not supported yet.", node.getNodeType()));
            };
        } else {
            return node;
        }
    }

    private BoundExpressionNode rewrite(BoundAwaitExpressionNode node) {
        StateBoundary boundary = newBoundary();
        add(new BoundSetGeneratorStateNode(boundary));
        add(new BoundSetGeneratorBoundaryNode(node.expression));
        currentBoundary = boundary;

        return new BoundGeneratorGetValueNode(node.type);
    }

    private BoundExpressionNode rewrite(BoundBinaryExpressionNode node) {
        LiftedVariable lVar = new LiftedVariable(new LocalVariable(null, node.left.type, null));
        LiftedVariable rVar = new LiftedVariable(new LocalVariable(null, node.right.type, null));

        storeExpressionValue(lVar, node.left);
        storeExpressionValue(rVar, node.right);

        return new BoundBinaryExpressionNode(
                new BoundNameExpressionNode(lVar, null),
                node.operator,
                new BoundNameExpressionNode(rVar, null),
                null);
    }

    private BoundExpressionNode rewrite(BoundMethodInvocationExpressionNode node) {
        assert !isAsync(node.objectReference);
        assert node.arguments.arguments.stream().anyMatch(this::isAsync);

        LiftedVariable[] variables = new LiftedVariable[node.arguments.arguments.size()];
        for (int i = 0; i < variables.length; i++) {
            BoundExpressionNode argument = node.arguments.arguments.get(i);
            variables[i] = new LiftedVariable(new LocalVariable(null, argument.type, null));
            storeExpressionValue(variables[i], argument);
        }

        return new BoundMethodInvocationExpressionNode(
                node.objectReference,
                node.method,
                new BoundArgumentsListNode(Arrays.stream(variables).map(v -> (BoundExpressionNode) new BoundNameExpressionNode(v, null)).toList(), null),
                node.refVariables,
                node.getRange());
    }

    private BoundExpressionNode rewrite(BoundUnaryExpressionNode node) {
        LiftedVariable variable = new LiftedVariable(new LocalVariable(null, node.operand.type, null));
        storeExpressionValue(variable, node.operand);
        return new BoundUnaryExpressionNode(
                node.operator,
                new BoundNameExpressionNode(variable, null),
                node.getRange());
    }

    private void storeExpressionValue(LiftedVariable variable, BoundExpressionNode expression) {
        if (isAsync(expression)) {
            BoundExpressionNode result = rewriteExpression(expression);
            add(new BoundVariableDeclarationNode(
                    createDummyTypeNode(),
                    new BoundNameExpressionNode(variable, null),
                    result,
                    null));
        } else {
            add(new BoundVariableDeclarationNode(
                    createDummyTypeNode(),
                    new BoundNameExpressionNode(variable, null),
                    expression,
                    null));
        }
    }

    private void markVariableDeclarations(BoundStatementNode statement) {
        statement.accept(new BinderTreeVisitor() {
            @Override
            public void visit(BoundVariableDeclarationNode node) {
                if (node.name.symbol instanceof LocalVariable local) {
                    local.setGeneratorState(currentBoundary);
                }
            }
        });
    }

    private void liftCrossBoundaryVariables(BoundStatementNode statement) {
        statement.accept(new BinderTreeVisitor() {
            @Override
            public void visit(BoundNameExpressionNode node) {
                if (node.symbol instanceof LocalVariable local) {
                    if (local.getGeneratorState() != currentBoundary) {
                        LiftedVariable lifted = new LiftedVariable(local);
                        for (BoundNameExpressionNode nameExpression : local.getReferences()) {
                            nameExpression.overrideSymbol(lifted);
                        }
                    }
                }
            }
        });
    }

    private void add(BoundStatementNode statement) {
        currentBoundary.statements.add(statement);
    }

    private StateBoundary newBoundary() {
        StateBoundary boundary = new StateBoundary(boundaries.size());
        boundaries.add(boundary);
        return boundary;
    }

    private void makeCurrent(StateBoundary boundary) {
        currentBoundary = boundary;
        boundary.index = boundaries.size();
        boundaries.add(boundary);
    }

    private BoundTypeNode createDummyTypeNode() {
        return new BoundInvalidTypeNode(null);
    }

    private boolean isAsync(BoundNode node) {
        AwaitVisitor visitor = new AwaitVisitor();
        node.accept(visitor);
        return visitor.isAsync();
    }
}