package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class WhileLoopStatementNode extends StatementNode {

    public final Token keyword;
    public final Token openParen;
    public final ExpressionNode condition;
    public final Token closeParen;
    public final StatementNode body;

    public WhileLoopStatementNode(Token keyword, Token openParen, ExpressionNode condition, Token closeParen, StatementNode body) {
        super(ParserNodeType.WHILE_LOOP_STATEMENT, TextRange.combine(keyword, body));
        this.keyword = keyword;
        this.openParen = openParen;
        this.condition = condition;
        this.closeParen = closeParen;
        this.body = body;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        condition.accept(visitor);
        body.accept(visitor);
    }
}