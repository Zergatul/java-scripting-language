package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundAwaitExpressionNode;

public class AsyncBinderTreeVisitor extends BinderTreeVisitor {

    private boolean isAsync;

    public boolean isAsync() {
        return isAsync;
    }

    @Override
    public void visit(BoundAwaitExpressionNode node) {
        isAsync = true;
    }
}