package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundLambdaExpressionNode;
import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;
import com.zergatul.scripting.compiler.CapturedAsyncStateMachineFieldVariable;
import com.zergatul.scripting.compiler.CapturedLocalVariable;

import java.util.ArrayList;
import java.util.List;

public class CapturedVariablesBinderTreeVisitor extends BinderTreeVisitor {

    public final List<CapturedLocalVariable> lambdaCaptured = new ArrayList<>();
    public final List<CapturedAsyncStateMachineFieldVariable> asyncCaptured = new ArrayList<>();

    @Override
    public void explicitVisit(BoundLambdaExpressionNode node) {
        // don't go inside lambdas
    }

    @Override
    public void visit(BoundNameExpressionNode node) {
        if (node.symbol instanceof CapturedLocalVariable variable) {
            if (!lambdaCaptured.contains(variable)) {
                lambdaCaptured.add(variable);
            }
        }
        if (node.symbol instanceof CapturedAsyncStateMachineFieldVariable variable) {
            if (!this.asyncCaptured.contains(variable)) {
                this.asyncCaptured.add(variable);
            }
        }
    }
}