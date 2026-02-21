package com.zergatul.scripting.generator;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.FallthroughFlow;
import com.zergatul.scripting.parser.AssignmentOperator;
import com.zergatul.scripting.type.SBoolean;
import com.zergatul.scripting.type.SInt;
import com.zergatul.scripting.visitors.AwaitVisitor;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.binding.nodes.BoundNodeType;
import com.zergatul.scripting.symbols.*;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BinderTreeGenerator {

    public final List<StateBoundary> boundaries = new ArrayList<>();

    private Frame frame;
    private StateBoundary currentBoundary;
    private boolean hasFinallyBlocks;
    private int maxPendingFinallyStates;

    public BinderTreeGenerator() {
        StateBoundary globalCatchState = new StateBoundary(null);
        boundaries.add(globalCatchState);

        frame = new AsyncTryBlockFrame(new FunctionFrame(), globalCatchState);
        currentBoundary = newBoundary();
    }

    public boolean hasFinallyBlocks() {
        return hasFinallyBlocks;
    }

    public int getMaxPendingFinallyStates() {
        return maxPendingFinallyStates;
    }

    public void generate(BoundStatementsListNode node) {
        for (BoundStatementNode statement : node.statements) {
            rewriteStatement(statement);
        }

        appendReturn();
    }

    private void appendReturn() {
        List<BoundStatementNode> statements = currentBoundary.statements;
        boolean append =
                statements.isEmpty() ||
                statements.getLast().getNodeType() != BoundNodeType.GENERATOR_RETURN;
        if (append) {
            statements.add(new BoundGeneratorReturnNode(null, frame.getCurrentFinallyState()));
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
                case TRY_STATEMENT -> rewriteAsync((BoundTryStatementNode) node);
                default -> throw new InternalException(String.format("Async %s not supported yet.", node.getNodeType()));
            }
        } else {
            processVariables(node);
            add(rewriteStatementSync(node));
        }
    }

    private BoundStatementNode rewriteStatementSync(BoundStatementNode node) {
        return switch (node.getNodeType()) {
            case BLOCK_STATEMENT -> rewriteSync((BoundBlockStatementNode) node);
            case IF_STATEMENT -> rewriteSync((BoundIfStatementNode) node);
            case FOR_LOOP_STATEMENT -> rewriteSync((BoundForLoopStatementNode) node);
            case FOREACH_LOOP_STATEMENT -> rewriteSync((BoundForEachLoopStatementNode) node);
            case WHILE_LOOP_STATEMENT -> rewriteSync((BoundWhileLoopStatementNode) node);
            case BREAK_STATEMENT -> rewriteSync((BoundBreakStatementNode) node);
            case CONTINUE_STATEMENT -> rewriteSync((BoundContinueStatementNode) node);
            case RETURN_STATEMENT -> rewriteSync((BoundReturnStatementNode) node);
            case TRY_STATEMENT -> rewriteSync((BoundTryStatementNode) node);
            case THROW_STATEMENT -> rewriteSync((BoundThrowStatementNode) node);
            default -> node;
        };
    }

    private BoundBlockStatementNode rewriteSync(BoundBlockStatementNode node) {
        return new BoundBlockStatementNode(node.statements.stream().map(this::rewriteStatementSync).toList());
    }

    private BoundIfStatementNode rewriteSync(BoundIfStatementNode node) {
        return new BoundIfStatementNode(
                node.condition,
                rewriteStatementSync(node.thenStatement),
                node.elseStatement != null ? rewriteStatementSync(node.elseStatement) : null,
                node.flow);
    }

    private BoundForLoopStatementNode rewriteSync(BoundForLoopStatementNode node) {
        frame = new SyncLoopFrame(frame);
        BoundStatementNode body = rewriteStatementSync(node.body);
        frame = frame.getParent();
        return node.withBody(body);
    }

    private BoundForEachLoopStatementNode rewriteSync(BoundForEachLoopStatementNode node) {
        frame = new SyncLoopFrame(frame);
        BoundStatementNode body = rewriteStatementSync(node.body);
        frame = frame.getParent();
        return node.withBody(body);
    }

    private BoundWhileLoopStatementNode rewriteSync(BoundWhileLoopStatementNode node) {
        frame = new SyncLoopFrame(frame);
        BoundStatementNode body = rewriteStatementSync(node.body);
        frame = frame.getParent();
        return new BoundWhileLoopStatementNode(node.condition, body);
    }

    private BoundStatementNode rewriteSync(BoundBreakStatementNode node) {
        LoopFrame loop = frame.getCurrentLoop();
        if (loop instanceof SyncLoopFrame) {
            return node;
        }

        AsyncLoopFrame asyncLoop = (AsyncLoopFrame) loop;
        List<AsyncTryFinallyBlockFrame> finallyFrames = frame.getFinallyFrames(asyncLoop);
        if (finallyFrames.isEmpty()) {
            return new BoundBlockStatementNode(
                    List.of(
                            new BoundSetGeneratorStateNode(asyncLoop.breakState),
                            new BoundGeneratorContinueNode()));
        } else {
            maxPendingFinallyStates = Math.max(maxPendingFinallyStates, finallyFrames.size());

            for (AsyncTryFinallyBlockFrame frame : finallyFrames) {
                frame.hasPendingFinallyJumps = true;
            }

            List<BoundStatementNode> statements = new ArrayList<>();
            statements.add(new BoundGeneratorAddPendingFinallyStateNode(asyncLoop.breakState));
            for (int i = finallyFrames.size() - 1; i >= 1; i--) {
                statements.add(new BoundGeneratorAddPendingFinallyStateNode(finallyFrames.get(i).finallyState));
            }
            statements.add(new BoundSetGeneratorStateNode(finallyFrames.getFirst().finallyState));
            statements.add(new BoundGeneratorContinueNode());
            return new BoundBlockStatementNode(statements);
        }
    }

    private BoundStatementNode rewriteSync(BoundContinueStatementNode node) {
        LoopFrame loop = frame.getCurrentLoop();
        if (loop instanceof SyncLoopFrame) {
            return node;
        }

        AsyncLoopFrame asyncLoop = (AsyncLoopFrame) loop;
        List<AsyncTryFinallyBlockFrame> finallyFrames = frame.getFinallyFrames(asyncLoop);
        if (finallyFrames.isEmpty()) {
            return new BoundBlockStatementNode(
                    List.of(
                            new BoundSetGeneratorStateNode(asyncLoop.continueState),
                            new BoundGeneratorContinueNode()));
        } else {
            maxPendingFinallyStates = Math.max(maxPendingFinallyStates, finallyFrames.size());

            List<BoundStatementNode> statements = new ArrayList<>();
            statements.add(new BoundGeneratorAddPendingFinallyStateNode(asyncLoop.continueState));
            for (int i = finallyFrames.size() - 1; i >= 1; i--) {
                statements.add(new BoundGeneratorAddPendingFinallyStateNode(finallyFrames.get(i).finallyState));
            }
            statements.add(new BoundSetGeneratorStateNode(finallyFrames.getFirst().finallyState));
            statements.add(new BoundGeneratorContinueNode());
            return new BoundBlockStatementNode(statements);
        }
    }

    private BoundStatementNode rewriteSync(BoundReturnStatementNode node) {
        if (frame instanceof AsyncCatchBlockFrame) {
            return new BoundBlockStatementNode(
                    List.of(
                            new BoundGeneratorForgetException(),
                            new BoundGeneratorReturnNode(node.expression, frame.getCurrentFinallyState())));
        } else {
            return new BoundGeneratorReturnNode(node.expression, frame.getCurrentFinallyState());
        }
    }

    private BoundTryStatementNode rewriteSync(BoundTryStatementNode node) {
        BoundBlockStatementNode tryBlock = rewriteSync(node.block);

        BoundBlockStatementNode catchBlock;
        if (node.catchBlock != null) {
            frame = new SyncCatchBlockFrame(frame);
            catchBlock = rewriteSync(node.catchBlock);
            frame = frame.getParent();
        } else {
            catchBlock = null;
        }

        BoundBlockStatementNode finallyBlock;
        if (node.finallyBlock != null) {
            finallyBlock = rewriteSync(node.finallyBlock);
        } else {
            finallyBlock = null;
        }

        return new BoundTryStatementNode(tryBlock, node.exceptionSymbol, catchBlock, finallyBlock);
    }

    private BoundStatementNode rewriteSync(BoundThrowStatementNode node) {
        if (node.expression != null) {
            return node;
        }

        if (frame instanceof SyncCatchBlockFrame) {
            return node;
        } else {
            return new BoundGeneratorRethrowNode();
        }
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
        StateBoundary thenTempBoundary = newDetachedBoundary();
        StateBoundary elseTempBoundary = newDetachedBoundary();
        StateBoundary end = newDetachedBoundary();

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
                elseBlock,
                FallthroughFlow.EMPTY));

        currentBoundary = end;
        boundaries.add(end);
    }

    private void rewriteAsync(BoundVariableDeclarationNode node) {
        assert node.expression != null;
        assert node.type != null;
        assert isAsync(node.expression);

        BoundExpressionNode expression = rewriteExpression(node.expression);
        add(new BoundVariableDeclarationNode(
                node.type,
                node.name,
                expression,
                node.getRange()));
    }

    private void rewriteAsync(BoundForLoopStatementNode node) {
        if (node.init != null) {
            rewriteStatement(node.init);
        }

        StateBoundary begin = newDetachedBoundary();
        StateBoundary cont = newDetachedBoundary();
        StateBoundary end = newDetachedBoundary();
        add(new BoundSetGeneratorStateNode(begin));

        makeCurrent(begin);

        if (node.condition != null) {
            BoundExpressionNode expression = rewriteExpression(node.condition);
            expression = new BoundUnaryExpressionNode(
                    new BoundUnaryOperatorNode(SBoolean.NOT.value()),
                    expression);
            add(new BoundIfStatementNode(
                    expression,
                    new BoundBlockStatementNode(new BoundSetGeneratorStateNode(end), new BoundGeneratorContinueNode()),
                    FallthroughFlow.EMPTY));
        }

        frame = new AsyncLoopFrame(frame, end, cont);
        rewriteStatement(node.body);
        frame = frame.getParent();

        add(new BoundSetGeneratorStateNode(cont));

        makeCurrent(cont);
        if (node.update != null) {
            rewriteStatement(node.update);
        }

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
                    iterable.getType().getInstanceProperties().stream().filter(p -> p.getName().equals("length")).findFirst().orElseThrow())));
        add(new BoundVariableDeclarationNode(new BoundNameExpressionNode(item)));

        StateBoundary begin = newDetachedBoundary();
        StateBoundary cont = newDetachedBoundary();
        StateBoundary end = newDetachedBoundary();
        add(new BoundSetGeneratorStateNode(begin));

        makeCurrent(begin);

        BoundExpressionNode condition = new BoundBinaryExpressionNode(
                new BoundNameExpressionNode(index),
                new BoundBinaryOperatorNode(SInt.GREATER_THAN_EQUALS.value()),
                new BoundNameExpressionNode(length));
        add(new BoundIfStatementNode(
                condition,
                new BoundBlockStatementNode(new BoundSetGeneratorStateNode(end), new BoundGeneratorContinueNode()),
                FallthroughFlow.EMPTY));

        add(new BoundAssignmentStatementNode(
                new BoundNameExpressionNode(item),
                new BoundAssignmentOperatorNode(AssignmentOperator.ASSIGNMENT),
                new BoundIndexExpressionNode(
                        new BoundNameExpressionNode(iterable),
                        new BoundNameExpressionNode(index),
                        iterable.getType().getIndexOperations().stream().filter(o -> o.indexType == SInt.instance).findFirst().orElseThrow())));

        frame = new AsyncLoopFrame(frame, end, cont);
        rewriteStatement(node.body);
        frame = frame.getParent();

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
        StateBoundary end = newDetachedBoundary();

        add(new BoundSetGeneratorStateNode(begin));
        currentBoundary = begin;

        BoundExpressionNode condition = rewriteExpression(node.condition);
        condition = new BoundUnaryExpressionNode(new BoundUnaryOperatorNode(SBoolean.NOT.value()), condition);
        add(new BoundIfStatementNode(
                condition,
                new BoundBlockStatementNode(new BoundSetGeneratorStateNode(end), new BoundGeneratorContinueNode()),
                FallthroughFlow.EMPTY));

        frame = new AsyncLoopFrame(frame, end, begin);
        rewriteStatement(node.body);
        frame = frame.getParent();

        add(new BoundSetGeneratorStateNode(begin));

        makeCurrent(end);
    }

    private void rewriteAsync(BoundReturnStatementNode node) {
        assert node.expression != null;

        BoundExpressionNode expression = rewriteExpression(node.expression);
        add(rewriteSync(new BoundReturnStatementNode(expression)));
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

    private void rewriteAsync(BoundTryStatementNode node) {
        hasFinallyBlocks |= node.finallyBlock != null;

        if (node.catchBlock != null && node.finallyBlock == null) {
            rewriteTryCatch(node);
        } else if (node.catchBlock == null && node.finallyBlock != null) {
            rewriteTryFinally(node);
        } else {
            throw new InternalException(); // TODO
        }
    }

    private void rewriteTryCatch(BoundTryStatementNode node) {
        assert node.catchBlock != null;

        if (node.exceptionSymbol != null) {
            Variable variable = node.exceptionSymbol.symbolRef.asVariable();
            node.exceptionSymbol.symbolRef.set(new AsyncExceptionVariable(variable));
        }

        StateBoundary catchBlockState = newDetachedBoundary();
        StateBoundary endState = newDetachedBoundary();
        frame = new AsyncTryBlockFrame(frame, catchBlockState);
        StateBoundary tryBlockState = newDetachedBoundary();

        add(new BoundSetGeneratorStateNode(tryBlockState));
        makeCurrent(tryBlockState);

        rewriteStatement(node.block);
        add(new BoundSetGeneratorStateNode(endState));

        frame = frame.getParent();

        makeCurrent(catchBlockState);
        frame = new AsyncCatchBlockFrame(frame);
        rewriteStatement(node.catchBlock);
        frame = frame.getParent();
        add(new BoundGeneratorForgetException());
        add(new BoundSetGeneratorStateNode(endState));

        makeCurrent(endState);
    }

    private void rewriteTryFinally(BoundTryStatementNode node) {
        assert node.finallyBlock != null;

        StateBoundary finallyBlockState = newDetachedBoundary();
        StateBoundary endState = newDetachedBoundary();
        AsyncTryFinallyBlockFrame finallyFrame = new AsyncTryFinallyBlockFrame(frame, finallyBlockState);
        frame = finallyFrame;
        StateBoundary tryBlockState = newDetachedBoundary();

        add(new BoundSetGeneratorStateNode(tryBlockState));
        makeCurrent(tryBlockState);

        rewriteStatement(node.block);
        add(new BoundSetGeneratorStateNode(finallyBlockState));

        frame = frame.getParent();

        makeCurrent(finallyBlockState);
        rewriteStatement(node.finallyBlock);

        if (finallyFrame.hasPendingFinallyJumps) {
            add(new BoundGeneratorFinallyDispatchNode());
        }

        StateBoundary outerFinally = frame.getCurrentFinallyState();
        if (outerFinally == null) {
            add(new BoundGeneratorFinallyExitNode(frame.getCurrentCatchState()));
        } else {
            add(new BoundSetGeneratorStateNode(outerFinally));
            add(new BoundGeneratorContinueNode());
        }
        add(new BoundSetGeneratorStateNode(endState));

        makeCurrent(endState);
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
        add(new BoundGeneratorAwaitTransitionNode(node.expression, boundary, frame.getClosestTryCatchFinallyState()));
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
        boolean isObjectReferenceAsync = isAsync(node.objectReference);
        boolean isArgumentsAsync = node.arguments.arguments.stream().anyMatch(this::isAsync);

        BoundExpressionNode objectRef;
        if (isObjectReferenceAsync) {
            LiftedVariable var = new LiftedVariable(new LocalVariable(null, node.objectReference.type, null));
            storeExpressionValue(var, node.objectReference);
            objectRef = new BoundNameExpressionNode(var);
        } else {
            objectRef = node.objectReference;
        }

        BoundArgumentsListNode arguments;
        if (isArgumentsAsync) {
            LiftedVariable[] variables = new LiftedVariable[node.arguments.arguments.size()];
            for (int i = 0; i < variables.length; i++) {
                BoundExpressionNode argument = node.arguments.arguments.get(i);
                variables[i] = new LiftedVariable(new LocalVariable(null, argument.type, null));
                storeExpressionValue(variables[i], argument);
            }
            arguments = new BoundArgumentsListNode(Arrays.stream(variables).map(v -> (BoundExpressionNode) new BoundNameExpressionNode(v)).toList());
        } else {
            arguments = node.arguments;
        }

        return new BoundMethodInvocationExpressionNode(
                objectRef,
                node.method,
                arguments,
                node.refVariables);
    }

    private BoundExpressionNode rewriteAsync(BoundUnaryExpressionNode node) {
        LiftedVariable variable = new LiftedVariable(new LocalVariable(null, node.operand.type, null));
        storeExpressionValue(variable, node.operand);
        return new BoundUnaryExpressionNode(
                node.syntaxNode,
                node.operator,
                new BoundNameExpressionNode(variable));
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

            @Override
            public void visit(BoundDeclarationPatternNode node) {
                if (node.symbolNode.symbolRef.get() instanceof LocalVariable local) {
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

    private StateBoundary newDetachedBoundary() {
        return new StateBoundary(frame.getClosestTryCatchFinallyState());
    }

    private StateBoundary newBoundary() {
        // TODO: if prev.statements.size = 0, reuse?
        StateBoundary boundary = newDetachedBoundary();
        boundaries.add(boundary);
        return boundary;
    }

    private void makeCurrent(StateBoundary boundary) {
        currentBoundary = boundary;
        boundaries.add(boundary);
    }

    private boolean isAsync(BoundNode node) {
        AwaitVisitor visitor = new AwaitVisitor();
        node.accept(visitor);
        return visitor.isAsync();
    }

    private static abstract class Frame {

        public final @Nullable Frame parent;

        protected Frame(@Nullable Frame parent) {
            this.parent = parent;
        }

        public Frame getParent() {
            return Objects.requireNonNull(parent);
        }

        public StateBoundary getClosestTryCatchFinallyState() {
            for (Frame frame = this; frame != null; frame = frame.parent) {
                if (frame instanceof AsyncTryBlockFrame asyncTryBlockFrame) {
                    return asyncTryBlockFrame.catchState;
                }
                if (frame instanceof AsyncTryFinallyBlockFrame asyncTryFinallyBlockFrame) {
                    return asyncTryFinallyBlockFrame.finallyState;
                }
            }
            throw new InternalException();
        }

        public StateBoundary getCurrentCatchState() {
            for (Frame frame = this; frame != null; frame = frame.parent) {
                if (frame instanceof AsyncTryBlockFrame asyncTryBlockFrame) {
                    return asyncTryBlockFrame.catchState;
                }
            }
            throw new InternalException();
        }

        public @Nullable StateBoundary getCurrentFinallyState() {
            for (Frame frame = this; frame != null; frame = frame.parent) {
                if (frame instanceof AsyncTryFinallyBlockFrame asyncTryFinallyBlockFrame) {
                    return asyncTryFinallyBlockFrame.finallyState;
                }
            }
            return null;
        }

        public List<AsyncTryFinallyBlockFrame> getFinallyFrames(Frame destination) {
            List<AsyncTryFinallyBlockFrame> frames = new ArrayList<>();
            for (Frame frame = this; frame != destination; frame = frame.getParent()) {
                if (frame instanceof AsyncTryFinallyBlockFrame finallyBlockFrame) {
                    frames.add(finallyBlockFrame);
                }
            }
            return frames;
        }

        public LoopFrame getCurrentLoop() {
            for (Frame frame = this; frame != null; frame = frame.parent) {
                if (frame instanceof LoopFrame loop) {
                    return loop;
                }
            }
            throw new InternalException();
        }
    }

    private static abstract class LoopFrame extends Frame {
        protected LoopFrame(Frame parent) {
            super(parent);
        }
    }

    private static class SyncLoopFrame extends LoopFrame {
        public SyncLoopFrame(Frame parent) {
            super(parent);
        }
    }

    private static class AsyncLoopFrame extends LoopFrame {

        public final StateBoundary breakState;
        public final StateBoundary continueState;

        public AsyncLoopFrame(Frame parent, StateBoundary breakState, StateBoundary continueState) {
            super(parent);
            this.breakState = breakState;
            this.continueState = continueState;
        }
    }

    private static class AsyncTryBlockFrame extends Frame {

        public final StateBoundary catchState;

        public AsyncTryBlockFrame(Frame parent, StateBoundary catchState) {
            super(parent);
            this.catchState = catchState;
        }
    }

    private static class AsyncCatchBlockFrame extends Frame {
        public AsyncCatchBlockFrame(Frame parent) {
            super(parent);
        }
    }

    private static class AsyncTryFinallyBlockFrame extends Frame {

        public final StateBoundary finallyState;
        public boolean hasPendingFinallyJumps;

        public AsyncTryFinallyBlockFrame(Frame parent, StateBoundary finallyState) {
            super(parent);
            this.finallyState = finallyState;
        }
    }

    private static class SyncCatchBlockFrame extends Frame {
        public SyncCatchBlockFrame(Frame parent) {
            super(parent);
        }
    }

    private static class FunctionFrame extends Frame {
        public FunctionFrame() {
            super(null);
        }
    }
}