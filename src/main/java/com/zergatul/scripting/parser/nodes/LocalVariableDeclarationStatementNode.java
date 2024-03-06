package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

public class LocalVariableDeclarationStatementNode extends StatementNode {

    public final TypeNode type;
    public final String identifier;
    public final ExpressionNode expression;

    public LocalVariableDeclarationStatementNode(TypeNode type, String identifier, ExpressionNode expression, TextRange range) {
        super(range);
        this.type = type;
        this.identifier = identifier;
        this.expression = expression;
    }
}