package com.zergatul.scripting.visitors;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.BoundAwaitExpressionNode;

public class AwaitVisitor extends BinderTreeVisitor {

    private boolean isAsync;

    public boolean isAsync() {
        return isAsync;
    }

    @Override
    public void visit(BoundAwaitExpressionNode node) {
        isAsync = true;
    }
}