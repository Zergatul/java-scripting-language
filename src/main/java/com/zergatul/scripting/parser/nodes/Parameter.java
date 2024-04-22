package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.lexer.IdentifierToken;

public class Parameter {

    private final TypeNode type;
    private final IdentifierToken identifier;

    public Parameter(TypeNode type, IdentifierToken identifier) {
        this.type = type;
        this.identifier = identifier;
    }

    public TypeNode getType() {
        return type;
    }

    public IdentifierToken getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Parameter other) {
            return other.type.equals(type) && other.identifier.equals(identifier);
        } else {
            return false;
        }
    }
}