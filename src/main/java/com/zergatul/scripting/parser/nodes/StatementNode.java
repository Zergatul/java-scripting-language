package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;

public abstract class StatementNode extends ParserNode {

    protected StatementNode(ParserNodeType nodeType, TextRange range) {
        super(nodeType, range);
    }

    public StatementNode updateWithSemicolon(Token semicolon) {
        throw new InternalException("Not supported.");
    }
}