package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class ForLoopStatementNode extends StatementNode {

    public final Token keyword;
    public final Token openParen;
    public final StatementNode init;
    public final ExpressionNode condition;
    public final StatementNode update;
    public final Token closeParen;
    public final StatementNode body;

    public ForLoopStatementNode(
            Token keyword,
            Token openParen,
            StatementNode init,
            ExpressionNode condition,
            StatementNode update,
            Token closeParen,
            StatementNode body
    ) {
        super(ParserNodeType.FOR_LOOP_STATEMENT, TextRange.combine(keyword, body));
        this.keyword = keyword;
        this.openParen = openParen;
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.closeParen = closeParen;
        this.body = body;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        init.accept(visitor);
        if (condition != null) {
            condition.accept(visitor);
        }
        update.accept(visitor);
        body.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(keyword, openParen, init, condition, update, closeParen, body);
    }
}