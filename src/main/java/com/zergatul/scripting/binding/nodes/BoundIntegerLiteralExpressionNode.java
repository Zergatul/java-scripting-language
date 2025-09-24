package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.nodes.IntegerLiteralExpressionNode;
import com.zergatul.scripting.type.SInt;

import java.util.List;

public class BoundIntegerLiteralExpressionNode extends BoundExpressionNode {

    public final ValueToken token;
    public final int value;

    public BoundIntegerLiteralExpressionNode(int value) {
        this(null, value, null);
    }

    public BoundIntegerLiteralExpressionNode(IntegerLiteralExpressionNode node, int value) {
        this(node.token, value, node.getRange());
    }

    public BoundIntegerLiteralExpressionNode(ValueToken token, int value, TextRange range) {
        super(BoundNodeType.INTEGER_LITERAL, SInt.instance, range);
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