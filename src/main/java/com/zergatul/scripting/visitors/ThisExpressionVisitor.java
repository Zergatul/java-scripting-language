package com.zergatul.scripting.visitors;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.BoundLambdaExpressionNode;
import com.zergatul.scripting.binding.nodes.BoundThisExpressionNode;
import com.zergatul.scripting.type.SType;

public class ThisExpressionVisitor extends BinderTreeVisitor {

    public boolean hasThis = false;
    public SType type;

    @Override
    public void explicitVisit(BoundLambdaExpressionNode node) {
        // don't go inside lambdas
    }

    @Override
    public void explicitVisit(BoundThisExpressionNode node) {
        hasThis = true;
        type = node.type;
    }
}