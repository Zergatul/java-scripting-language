package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.nodes.FloatLiteralExpressionNode;
import com.zergatul.scripting.type.SFloat;

import java.util.List;

public class BoundFloatLiteralExpressionNode extends BoundExpressionNode {

    public final ValueToken token;
    public final double value;

    public BoundFloatLiteralExpressionNode(FloatLiteralExpressionNode node, double value) {
        this(node.token, value, node.getRange());
    }

    public BoundFloatLiteralExpressionNode(ValueToken token, double value, TextRange range) {
        super(BoundNodeType.FLOAT_LITERAL, SFloat.instance, range);
        this.token = token;
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