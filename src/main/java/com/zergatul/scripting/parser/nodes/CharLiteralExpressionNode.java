package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class CharLiteralExpressionNode extends ExpressionNode {

    public final char value;

    public CharLiteralExpressionNode(ValueToken token) {
        super(NodeType.CHAR_LITERAL, token.getRange());
        this.value = token.value.charAt(0);
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}
}