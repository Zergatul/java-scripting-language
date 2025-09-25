package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.Integer64LiteralExpressionNode;
import com.zergatul.scripting.type.SInt64;

import java.util.List;

public class BoundInteger64LiteralExpressionNode extends BoundExpressionNode {

    public final Integer64LiteralExpressionNode syntaxNode;
    public final long value;

    public BoundInteger64LiteralExpressionNode(Integer64LiteralExpressionNode node, long value) {
        this(node, value, node.getRange());
    }

    public BoundInteger64LiteralExpressionNode(Integer64LiteralExpressionNode node, long value, TextRange range) {
        super(BoundNodeType.INTEGER64_LITERAL, SInt64.instance, range);
        this.syntaxNode = node;
        this.value = value;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}