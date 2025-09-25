package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.FloatLiteralExpressionNode;
import com.zergatul.scripting.type.SFloat;

import java.util.List;

public class BoundFloatLiteralExpressionNode extends BoundExpressionNode {

    public final FloatLiteralExpressionNode syntaxNode;
    public final double value;

    public BoundFloatLiteralExpressionNode(FloatLiteralExpressionNode node, double value) {
        this(node, value, node.getRange());
    }

    public BoundFloatLiteralExpressionNode(FloatLiteralExpressionNode node, double value, TextRange range) {
        super(BoundNodeType.FLOAT_LITERAL, SFloat.instance, range);
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