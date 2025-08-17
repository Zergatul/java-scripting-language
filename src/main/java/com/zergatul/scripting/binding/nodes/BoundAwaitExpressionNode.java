package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundAwaitExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode expression;

    public BoundAwaitExpressionNode(BoundExpressionNode expression, SType type, TextRange range) {
        super(NodeType.AWAIT_EXPRESSION, type, range);
        this.expression = expression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public BoundNode accept(BinderTreeRewriter rewriter) {
        return rewriter.visit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        expression.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression);
    }

    public BoundAwaitExpressionNode update(BoundExpressionNode expression) {
        if (this.expression != expression) {
            return new BoundAwaitExpressionNode(expression, type, getRange());
        } else {
            return this;
        }
    }
}