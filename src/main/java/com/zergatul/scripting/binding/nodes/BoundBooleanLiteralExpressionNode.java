package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.BooleanLiteralExpressionNode;
import com.zergatul.scripting.type.SBoolean;

import java.util.List;

public class BoundBooleanLiteralExpressionNode extends BoundExpressionNode {

    public final Token token;
    public final boolean value;

    public BoundBooleanLiteralExpressionNode(BooleanLiteralExpressionNode node) {
        this(node.token, node.value, node.getRange());
    }

    public BoundBooleanLiteralExpressionNode(Token token, boolean value, TextRange range) {
        super(BoundNodeType.BOOLEAN_LITERAL, SBoolean.instance, range);
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