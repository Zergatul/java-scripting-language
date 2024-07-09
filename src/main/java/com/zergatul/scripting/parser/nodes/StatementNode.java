package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;

public abstract class StatementNode extends Node {

    protected StatementNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }

    public abstract boolean isAsync();

    public StatementNode prepend(Token token) {
        throw new InternalException("Not supported.");
    }

    public StatementNode append(Token token) {
        throw new InternalException("Not supported.");
    }
}