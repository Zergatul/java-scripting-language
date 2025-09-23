package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class PostfixStatementNode extends StatementNode {

    public final ExpressionNode expression;
    public final Token operation;
    public final Token semicolon;

    public PostfixStatementNode(ParserNodeType nodeType, ExpressionNode expression, Token operation, Token semicolon) {
        super(nodeType, TextRange.combine(expression, semicolon != null ? semicolon : operation));
        this.expression = expression;
        this.operation = operation;
        this.semicolon = semicolon;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        expression.accept(visitor);
    }

    @Override
    public StatementNode updateWithSemicolon(Token semicolon) {
        return new PostfixStatementNode(getNodeType(), expression, operation, semicolon);
    }
}