package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ParenthesizedExpressionNode;

import java.util.List;

public class BoundParenthesizedExpressionNode extends BoundExpressionNode {

    public final ParenthesizedExpressionNode syntaxNode;
    public final BoundExpressionNode inner;

    public BoundParenthesizedExpressionNode(ParenthesizedExpressionNode node, BoundExpressionNode inner) {
        this(node, inner, node.getRange());
    }

    public BoundParenthesizedExpressionNode(ParenthesizedExpressionNode node, BoundExpressionNode inner, TextRange range) {
        super(BoundNodeType.PARENTHESIZED_EXPRESSION, inner.type, range);
        this.syntaxNode = node;
        this.inner = inner;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        inner.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(inner);
    }
}