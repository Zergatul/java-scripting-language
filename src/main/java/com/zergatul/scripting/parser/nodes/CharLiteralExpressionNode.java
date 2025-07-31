package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.lexer.CharToken;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class CharLiteralExpressionNode extends ExpressionNode {

    public final char value;

    public CharLiteralExpressionNode(CharToken token) {
        super(NodeType.CHAR_LITERAL, token.getRange());
        this.value = token.value;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}
}