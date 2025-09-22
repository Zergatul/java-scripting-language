package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ExpressionStatementNode extends StatementNode {

    public final ExpressionNode expression;
    public final Token semicolon;

    public ExpressionStatementNode(ExpressionNode expression, Token semicolon) {
        super(ParserNodeType.EXPRESSION_STATEMENT, semicolon == null ? expression.getRange() : TextRange.combine(expression, semicolon));
        this.expression = expression;
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
        return new ExpressionStatementNode(expression, semicolon);
    }
}