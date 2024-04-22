package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.IdentifierToken;
import com.zergatul.scripting.parser.NodeType;

public class FunctionNode extends Node {

    public final TypeNode returnType;
    public final IdentifierToken identifier;
    public final ParameterListNode parameters;
    public final BlockStatementNode body;

    public FunctionNode(TypeNode returnType, IdentifierToken identifier, ParameterListNode parameters, BlockStatementNode body, TextRange range) {
        super(NodeType.FUNCTION, range);
        this.returnType = returnType;
        this.identifier = identifier;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FunctionNode other) {
            return  other.returnType.equals(returnType) &&
                    other.identifier.equals(identifier) &&
                    other.parameters.equals(parameters) &&
                    other.body.equals(body) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}