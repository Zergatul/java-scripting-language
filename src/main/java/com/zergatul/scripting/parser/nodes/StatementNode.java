package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;

public abstract class StatementNode extends Node {

    protected StatementNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }

    public StatementNode updateWithSemicolon(Token semicolon) {
        throw new InternalException("Not supported.");
    }
}