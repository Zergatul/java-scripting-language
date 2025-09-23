package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.nodes.CharLiteralExpressionNode;
import com.zergatul.scripting.type.SChar;

import java.util.List;

public class BoundCharLiteralExpressionNode extends BoundExpressionNode {

    public final ValueToken token;
    public final char value;

    public BoundCharLiteralExpressionNode(CharLiteralExpressionNode node) {
        this(node.token, node.value, node.getRange());
    }

    public BoundCharLiteralExpressionNode(ValueToken token, char value, TextRange range) {
        super(BoundNodeType.CHAR_LITERAL, SChar.instance, range);
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