package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ForLoopStatementNode extends StatementNode {

    public final Token keyword;
    public final Token openParen;
    @Nullable public final StatementNode init;
    public final Token semicolon1;
    @Nullable public final ExpressionNode condition;
    public final Token semicolon2;
    @Nullable public final StatementNode update;
    public final Token closeParen;
    public final StatementNode body;

    public ForLoopStatementNode(
            Token keyword,
            Token openParen,
            @Nullable StatementNode init,
            Token semicolon1,
            @Nullable ExpressionNode condition,
            Token semicolon2,
            @Nullable StatementNode update,
            Token closeParen,
            StatementNode body
    ) {
        super(ParserNodeType.FOR_LOOP_STATEMENT, TextRange.combine(keyword, body));
        this.keyword = keyword;
        this.openParen = openParen;
        this.init = init;
        this.semicolon1 = semicolon1;
        this.condition = condition;
        this.semicolon2 = semicolon2;
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
        if (init != null) {
            init.accept(visitor);
        }
        if (condition != null) {
            condition.accept(visitor);
        }
        if (update != null) {
            update.accept(visitor);
        }
        body.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> nodes = new ArrayList<>();
        nodes.add(keyword);
        nodes.add(openParen);
        if (init != null) {
            nodes.add(init);
        }
        nodes.add(semicolon1);
        if (condition != null) {
            nodes.add(condition);
        }
        nodes.add(semicolon2);
        if (update != null) {
            nodes.add(update);
        }
        nodes.add(closeParen);
        nodes.add(body);
        return nodes;
    }
}