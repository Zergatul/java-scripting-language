package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.Objects;

public class ForLoopStatementNode extends StatementNode {

    public final Token lParen;
    public final Token rParen;
    public final StatementNode init;
    public final ExpressionNode condition;
    public final StatementNode update;
    public final StatementNode body;

    public ForLoopStatementNode(Token lParen, Token rParen, StatementNode init, ExpressionNode condition, StatementNode update, StatementNode body, TextRange range) {
        super(NodeType.FOR_LOOP_STATEMENT, range);
        this.lParen = lParen;
        this.rParen = rParen;
        this.init = init;
        this.condition = condition;
        this.update = update;
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
    public boolean equals(Object obj) {
        if (obj instanceof ForLoopStatementNode other) {
            return  other.lParen.equals(lParen) &&
                    other.rParen.equals(rParen) &&
                    other.init.equals(init) &&
                    Objects.equals(other.condition, condition) &&
                    other.update.equals(update) &&
                    other.body.equals(body) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}