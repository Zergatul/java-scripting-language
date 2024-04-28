package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.CharToken;
import com.zergatul.scripting.parser.NodeType;

public class CharLiteralExpressionNode extends ExpressionNode {

    public final String value;

    public CharLiteralExpressionNode(CharToken token) {
        this(token.value, token.getRange());
    }

    protected CharLiteralExpressionNode(String value, TextRange range) {
        super(NodeType.CHAR_LITERAL, range);
        this.value = value;
    }
}