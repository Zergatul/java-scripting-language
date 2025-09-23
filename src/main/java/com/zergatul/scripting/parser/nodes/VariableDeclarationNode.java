package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class VariableDeclarationNode extends StatementNode {

    public final TypeNode type;
    public final NameExpressionNode name;
    public final Token equal;
    public final ExpressionNode expression;
    public final Token semicolon;

    public VariableDeclarationNode(TypeNode type, NameExpressionNode name, Token equal, ExpressionNode expression, Token semicolon) {
        super(ParserNodeType.VARIABLE_DECLARATION, TextRange.combine(type, semicolon));
        this.type = type;
        this.name = name;
        this.equal = equal;
        this.expression = expression;
        this.semicolon = semicolon;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public boolean isOpen() {
        return (expression != null && expression.isMissing()) || name.isMissing();
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        type.accept(visitor);
        name.accept(visitor);
        if (expression != null) {
            expression.accept(visitor);
        }
    }

    @Override
    public StatementNode updateWithSemicolon(Token semicolon) {
        return new VariableDeclarationNode(type, name, equal, expression, semicolon);
    }
}