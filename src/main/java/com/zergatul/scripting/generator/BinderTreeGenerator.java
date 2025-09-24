package com.zergatul.scripting.generator;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.parser.AssignmentOperator;
import com.zergatul.scripting.type.SBoolean;
import com.zergatul.scripting.type.SInt;
import com.zergatul.scripting.visitors.AwaitVisitor;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.binding.nodes.BoundNodeType;
import com.zergatul.scripting.symbols.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BinderTreeGenerator {

    public final List<StateBoundary> boundaries = new ArrayList<>();

    private StateBoundary currentBoundary;

    public void generate(BoundStatementsListNode node) {
        currentBoundary = newBoundary();
        for (BoundStatementNode statement : node.statements) {
            rewriteStatement(statement);
        }

        appendReturn();
    }

    private void appendReturn() {
        List<BoundStatementNode> statements = currentBoundary.statements;
        boolean append =
                statements.isEmpty() ||
                statements.get(statements.size() - 1).getNodeType() != BoundNodeType.GENERATOR_RETURN;
        if (append) {
            statements.add(new BoundGeneratorReturnNode(null));
        }
    }

    private void rewriteStatement(BoundStatementNode node) {
        if (isAsync(node)) {
            switch (node.getNodeType()) {
                case AUGMENTED_ASSIGNMENT_STATEMENT -> rewriteAsync((BoundAugmentedAssignmentStatementNode) node);
                case BLOCK_STATEMENT -> rewriteAsync((BoundBlockStatementNode) node);
                case EXPRESSION_STATEMENT -> rewriteAsync((BoundExpressionStatementNode) node);
                case FOR_LOOP_STATEMENT -> rewriteAsync((BoundForLoopStatementNode) node);
                case FOREACH_LOOP_STATEMENT -> rewriteAsync((BoundForEachLoopStatementNode) node);
                case IF_STATEMENT -> rewriteAsync((BoundIfStatementNode) node);
                case VARIABLE_DECLARATION -> rewriteAsync((BoundVariableDeclarationNode) node);
                case WHILE_LOOP_STATEMENT -> rewriteAsync((BoundWhileLoopStatementNode) node);
                case RETURN_STATEMENT -> rewriteAsync((BoundReturnStatementNode) node);
                default -> throw new InternalException(String.format("Async %s not supported yet.", node.getNodeType()));
            }
        } else {
            processVariables(node);
            add(rewriteStatementSync(node));
        }
    }

    private BoundStatementNode rewriteStatementSync(BoundStatementNode node) {
        return switch (node.getNodeType()) {
            case BLOCK_STATEMENT -> rewrite((BoundBlockStatementNode) node);
            case IF_STATEMENT -> rewrite((BoundIfStatementNode) node);
            case FOR_LOOP_STATEMENT -> rewrite((BoundForLoopStatementNode) node);
            case FOREACH_LOOP_STATEMENT -> rewrite((BoundForEachLoopStatementNode) node);
            case WHILE_LOOP_STATEMENT -> rewrite((BoundWhileLoopStatementNode) node);
            case RETURN_STATEMENT -> rewrite((BoundReturnStatementNode) node);
            default -> node;
        };
    }

    private BoundBlockStatementNode rewrite(BoundBlockStatementNode node) {
        return new BoundBlockStatementNode(node.statements.stream().map(this::rewriteStatementSync).toList());
    }

    private BoundIfStatementNode rewrite(BoundIfStatementNode node) {
        return new BoundIfStatementNode(
                node.condition,
                rewriteStatementSync(node.thenStatement),
                node.elseStatement != null ? rewriteStatementSync(node.elseStatement) : null);
    }

    private BoundForLoopStatementNode rewrite(BoundForLoopStatementNode node) {
        return node.withBody(rewriteStatementSync(node.body));
    }

    private BoundForEachLoopStatementNode rewrite(BoundForEachLoopStatementNode node) {
        return node.withBody(rewriteStatementSync(node.body));
    }

    private BoundWhileLoopStatementNode rewrite(BoundWhileLoopStatementNode node) {
        return new BoundWhileLoopStatementNode(
                node.condition,
                rewriteStatementSync(node.body));
    }

    private BoundGeneratorReturnNode rewrite(BoundReturnStatementNode node) {
        return new BoundGeneratorReturnNode(node.expression);
    }

    private void rewriteAsync(BoundBlockStatementNode node) {
        for (BoundStatementNode statement : node.statements) {
            rewriteStatement(statement);
        }
    }

    private void rewriteAsync(BoundExpressionStatementNode node) {
        BoundExpressionNode expression = rewriteExpression(node.expression);
        add(new BoundExpressionStatementNode(expression));
    }

    private void rewriteAsync(BoundIfStatementNode node) {
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
                new BoundNameExpressionNode(condition),
                thenBlock,
                elseBlock));

        currentBoundary = end;
        end.index = boundaries.size();
        boundaries.add(end);
    }

    private void rewriteAsync(BoundVariableDeclarationNode node) {
        assert node.expression != null;
        assert isAsync(node.expression);

        BoundExpressionNode expression = rewriteExpression(node.expression);
        add(new BoundVariableDeclarationNode(
                node.type,
                node.name,
                expression,
                node.getRange()));
    }

    private void rewriteAsync(BoundForLoopStatementNode node) {
        rewriteStatement(node.init);

        StateBoundary begin = new StateBoundary();
        StateBoundary cont = new StateBoundary();
        StateBoundary end = new StateBoundary();
        add(new BoundSetGeneratorStateNode(begin));

        makeCurrent(begin);

        if (node.condition != null) {
            BoundExpressionNode expression = rewriteExpression(node.condition);
            expression = new BoundUnaryExpressionNode(
                    new BoundUnaryOperatorNode(SBoolean.instance.not()),
                    expression);
            add(new BoundIfStatementNode(
                    expression,
                    new BoundBlockStatementNode(new BoundSetGeneratorStateNode(end), new BoundGeneratorContinueNode())));
        }

        LoopBodyTransformer transformer = new LoopBodyTransformer(node.body, cont, end);
        rewriteStatement(transformer.process());
        add(new BoundSetGeneratorStateNode(cont));

        makeCurrent(cont);
        rewriteStatement(node.update);

        add(new BoundSetGeneratorStateNode(begin));

        makeCurrent(end);
    }

    private void rewriteAsync(BoundForEachLoopStatementNode node) {
        BoundExpressionNode iterableExpression = rewriteExpression(node.iterable);
        LiftedVariable iterable = new LiftedVariable(new LocalVariable(null, node.iterable.type, null));
        LiftedVariable index = new LiftedVariable(node.index.asLocalVariable());
        LiftedVariable length = new LiftedVariable(node.length.asLocalVariable());
        LiftedVariable item;
        if (node.name.getSymbol() instanceof LiftedVariable lifted) {
            item = lifted;
        } else {
            item = new LiftedVariable((LocalVariable) node.name.getSymbol());
            node.name.symbolRef.set(item);
        }

        add(new BoundVariableDeclarationNode(new BoundNameExpressionNode(iterable), iterableExpression));
        add(new BoundVariableDeclarationNode(new BoundNameExpressionNode(index), new BoundIntegerLiteralExpressionNode(0)));
        add(new BoundVariableDeclarationNode(
                new BoundNameExpressionNode(length),
                new BoundPropertyAccessExpressionNode(
                    new BoundNameExpressionNode(iterable),
                    iterable.getType().getInstanceProperty("length"))));
        add(new BoundVariableDeclarationNode(new BoundNameExpressionNode(item)));

        StateBoundary begin = new StateBoundary();
        StateBoundary cont = new StateBoundary();
        StateBoundary end = new StateBoundary();
        add(new BoundSetGeneratorStateNode(begin));

        makeCurrent(begin);

        BoundExpressionNode condition = new BoundBinaryExpressionNode(
                new BoundNameExpressionNode(index),
                new BoundBinaryOperatorNode(SInt.instance.greaterEquals(SInt.instance)),
                new BoundNameExpressionNode(length));
        add(new BoundIfStatementNode(
                condition,
                new BoundBlockStatementNode(new BoundSetGeneratorStateNode(end), new BoundGeneratorContinueNode())));

        add(new BoundAssignmentStatementNode(
                new BoundNameExpressionNode(item),
                new BoundAssignmentOperatorNode(AssignmentOperator.ASSIGNMENT),
                new BoundIndexExpressionNode(
                        new BoundNameExpressionNode(iterable),
                        new BoundNameExpressionNode(index),
                        iterable.getType().index(SInt.instance))));

        LoopBodyTransformer transformer = new LoopBodyTransformer(node.body, cont, end);
        rewriteStatement(transformer.process());
        add(new BoundSetGeneratorStateNode(cont));

        makeCurrent(cont);
        add(new BoundPostfixStatementNode(
                BoundNodeType.INCREMENT_STATEMENT,
                new BoundNameExpressionNode(index),
                SInt.instance.increment()));

        add(new BoundSetGeneratorStateNode(begin));

        makeCurrent(end);
    }

    private void rewriteAsync(BoundWhileLoopStatementNode node) {
        StateBoundary begin = newBoundary();
        StateBoundary end = new StateBoundary();

        add(new BoundSetGeneratorStateNode(begin));
        currentBoundary = begin;

        BoundExpressionNode condition = rewriteExpression(node.condition);
        condition = new BoundUnaryExpressionNode(new BoundUnaryOperatorNode(SBoolean.instance.not()), condition);
        add(new BoundIfStatementNode(
                condition,
                new BoundBlockStatementNode(new BoundSetGeneratorStateNode(end), new BoundGeneratorContinueNode())));

        LoopBodyTransformer transformer = new LoopBodyTransformer(node.body, begin, end);
        rewriteStatement(transformer.process());
        add(new BoundSetGeneratorStateNode(begin));

        makeCurrent(end);
    }

    private void rewriteAsync(BoundReturnStatementNode node) {
        assert node.expression != null;

        BoundExpressionNode expression = rewriteExpression(node.expression);
        add(rewrite(new BoundReturnStatementNode(expression)));
    }

    private void rewriteAsync(BoundAugmentedAssignmentStatementNode node) {
        if (isAsync(node.left)) {
            throw new InternalException("Async left side augmented assignment is not supported yet.");
        }

        BoundExpressionNode expression = rewriteExpression(node.right);
        add(new BoundAugmentedAssignmentStatementNode(
                node.left,
                node.assignmentOperator,
                node.operation,
                expression));
    }

    private BoundExpressionNode rewriteExpression(BoundExpressionNode node) {
        if (isAsync(node)) {
            return switch (node.getNodeType()) {
                case AWAIT_EXPRESSION -> rewriteAsync((BoundAwaitExpressionNode) node);
                case PARENTHESIZED_EXPRESSION -> rewriteAsync((BoundParenthesizedExpressionNode) node);
                case BINARY_EXPRESSION -> rewriteAsync((BoundBinaryExpressionNode) node);
                case METHOD_INVOCATION_EXPRESSION -> rewriteAsync((BoundMethodInvocationExpressionNode) node);
                case UNARY_EXPRESSION -> rewriteAsync((BoundUnaryExpressionNode) node);
                case IMPLICIT_CAST -> rewriteAsync((BoundImplicitCastExpressionNode) node);
                case CONVERSION -> rewriteAsync((BoundConversionNode) node);
                default -> throw new InternalException(String.format("Async %s not supported yet.", node.getNodeType()));
            };
        } else {
            processVariables(node);
            return node;
        }
    }

    private BoundExpressionNode rewriteAsync(BoundAwaitExpressionNode node) {
        StateBoundary boundary = newBoundary();
        add(new BoundSetGeneratorStateNode(boundary));
        add(new BoundSetGeneratorBoundaryNode(node.expression));
        currentBoundary = boundary;

        return new BoundGeneratorGetValueNode(node.type);
    }

    public BoundExpressionNode rewriteAsync(BoundParenthesizedExpressionNode node) {
        return rewriteExpression(node.inner);
    }

    private BoundExpressionNode rewriteAsync(BoundBinaryExpressionNode node) {
        LiftedVariable lVar = new LiftedVariable(new LocalVariable(null, node.left.type, null));
        LiftedVariable rVar = new LiftedVariable(new LocalVariable(null, node.right.type, null));

        storeExpressionValue(lVar, node.left);
        storeExpressionValue(rVar, node.right);

        return new BoundBinaryExpressionNode(
                new BoundNameExpressionNode(lVar),
                node.operator,
                new BoundNameExpressionNode(rVar));
    }

    private BoundExpressionNode rewriteAsync(BoundMethodInvocationExpressionNode node) {
        assert !isAsync(node.objectReference);
        assert node.arguments.arguments.getNodes().stream().anyMatch(this::isAsync);

        LiftedVariable[] variables = new LiftedVariable[node.arguments.arguments.size()];
        for (int i = 0; i < variables.length; i++) {
            BoundExpressionNode argument = node.arguments.arguments.getNodeAt(i);
            variables[i] = new LiftedVariable(new LocalVariable(null, argument.type, null));
            storeExpressionValue(variables[i], argument);
        }

        return new BoundMethodInvocationExpressionNode(
                node.objectReference,
                node.dot,
                node.method,
                new BoundArgumentsListNode(BoundSeparatedList.of(Arrays.stream(variables).map(v -> (BoundExpressionNode) new BoundNameExpressionNode(v)).toList())),
                node.refVariables,
                node.getRange());
    }

    private BoundExpressionNode rewriteAsync(BoundUnaryExpressionNode node) {
        LiftedVariable variable = new LiftedVariable(new LocalVariable(null, node.operand.type, null));
        storeExpressionValue(variable, node.operand);
        return new BoundUnaryExpressionNode(
                node.operator,
                new BoundNameExpressionNode(variable),
                node.getRange());
    }

    private BoundExpressionNode rewriteAsync(BoundImplicitCastExpressionNode node) {
        LiftedVariable variable = new LiftedVariable(new LocalVariable(null, node.operand.type, null));
        storeExpressionValue(variable, node.operand);
        return new BoundImplicitCastExpressionNode(
                new BoundNameExpressionNode(variable),
                node.operation);
    }

    private BoundExpressionNode rewriteAsync(BoundConversionNode node) {
        LiftedVariable variable = new LiftedVariable(new LocalVariable(null, node.expression.type, null));
        storeExpressionValue(variable, node.expression);
        return new BoundConversionNode(
                new BoundNameExpressionNode(variable),
                node.conversionInfo,
                node.type,
                node.getRange());
    }

    private void storeExpressionValue(LiftedVariable variable, BoundExpressionNode expression) {
        if (isAsync(expression)) {
            BoundExpressionNode result = rewriteExpression(expression);
            add(new BoundVariableDeclarationNode(new BoundNameExpressionNode(variable), result));
        } else {
            processVariables(expression);
            add(new BoundVariableDeclarationNode(new BoundNameExpressionNode(variable), expression));
        }
    }

    private void processVariables(BoundNode node) {
        markVariableDeclarations(node);
        liftCrossBoundaryVariables(node);
    }

    private void markVariableDeclarations(BoundNode node) {
        node.accept(new BinderTreeVisitor() {

            @Override
            public void explicitVisit(BoundLambdaExpressionNode node) {
                // don't jump inside
            }

            @Override
            public void visit(BoundVariableDeclarationNode node) {
                if (node.name.getSymbol() instanceof LocalVariable local) {
                    local.setGeneratorState(currentBoundary);
                }
            }

            @Override
            public void visit(BoundForEachLoopStatementNode node) {
                if (node.name.getSymbol() instanceof LocalVariable local) {
                    local.setGeneratorState(currentBoundary);
                }
            }
        });
    }

    private void liftCrossBoundaryVariables(BoundNode node) {
        node.accept(new BinderTreeVisitor() {

            @Override
            public void explicitVisit(BoundLambdaExpressionNode node) {
                // don't jump inside
            }

            @Override
            public void visit(BoundNameExpressionNode node) {
                if (node.getSymbol() instanceof LocalVariable local) {
                    if (local.getGeneratorState() != currentBoundary) {
                        LiftedVariable lifted = new LiftedVariable(local);
                        node.symbolRef.set(lifted);
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

    private boolean isAsync(BoundNode node) {
        AwaitVisitor visitor = new AwaitVisitor();
        node.accept(visitor);
        return visitor.isAsync();
    }
}