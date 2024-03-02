package com.zergatul.scripting.parser.nodes;

import java.util.Objects;

public class VariableDeclarationNode extends StatementNode {

    public final TypeNode type;
    public final String identifier;
    public final ExpressionNode expression;

    public VariableDeclarationNode(TypeNode type, String identifier) {
        this(type, identifier, null);
    }

    public VariableDeclarationNode(TypeNode type, String identifier, ExpressionNode expression) {
        this.type = type;
        this.identifier = identifier;
        this.expression = expression;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VariableDeclarationNode other) {
            return other.type.equals(type) && other.identifier.equals(identifier) && Objects.equals(other.expression, expression);
        } else {
            return false;
        }
    }
}