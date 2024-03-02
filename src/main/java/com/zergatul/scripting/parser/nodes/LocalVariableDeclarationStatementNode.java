package com.zergatul.scripting.parser.nodes;

public class LocalVariableDeclarationStatementNode extends StatementNode {

    public final TypeNode type;
    public final String identifier;
    public final ExpressionNode expression;

    public LocalVariableDeclarationStatementNode(TypeNode type, String identifier, ExpressionNode expression) {
        this.type = type;
        this.identifier = identifier;
        this.expression = expression;
    }
}