package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ReturnStatementNode extends StatementNode {

    public final Token keyword;
    public final ExpressionNode expression;
    public final Token semicolon;

    public ReturnStatementNode(Token keyword, ExpressionNode expression, Token semicolon) {
        super(ParserNodeType.RETURN_STATEMENT, TextRange.combine(keyword, semicolon));
        this.keyword = keyword;
        this.expression = expression;
        this.semicolon = semicolon;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        if (expression != null) {
            expression.accept(visitor);
        }
    }
}