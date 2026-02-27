package com.zergatul.scripting.visitors;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.*;

// check if loop body has break/continue inside async try-statements with finally block
public class LoopControlFlowVisitor extends BinderTreeVisitor {

    private boolean hasBreak;
    private boolean hasContinue;
    private boolean insideTryStatement;

    public boolean hasBreak() {
        return hasBreak;
    }

    public boolean hasContinue() {
        return hasContinue;
    }

    @Override
    public void explicitVisit(BoundForEachLoopStatementNode node) {
        // don't scan inner loops
    }

    @Override
    public void explicitVisit(BoundForLoopStatementNode node) {
        // don't scan inner loops
    }

    @Override
    public void explicitVisit(BoundLambdaExpressionNode node) {
        // don't scan lambda
    }

    @Override
    public void explicitVisit(BoundTryStatementNode node) {
        boolean apply = !insideTryStatement;
        if (apply) {
            AwaitVisitor visitor = new AwaitVisitor();
            node.accept(visitor);
            apply = visitor.isAsync();
        }

        if (apply) {
            insideTryStatement = true;
        }

        super.explicitVisit(node);

        if (apply) {
            insideTryStatement = false;
        }
    }

    @Override
    public void explicitVisit(BoundWhileLoopStatementNode node) {
        // don't scan inner loops
    }

    @Override
    public void visit(BoundBreakStatementNode node) {
        if (insideTryStatement) {
            hasBreak = true;
        }
    }

    @Override
    public void visit(BoundContinueStatementNode node) {
        if (insideTryStatement) {
            hasContinue = true;
        }
    }
}