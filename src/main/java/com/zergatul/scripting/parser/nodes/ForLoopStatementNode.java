package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.Objects;

public class ForLoopStatementNode extends StatementNode {

    public final Token openParenthesis;
    public final StatementNode init;
    public final ExpressionNode condition;
    public final StatementNode update;
    public final Token closeParenthesis;
    public final StatementNode body;

    public ForLoopStatementNode(Token openParenthesis, StatementNode init, ExpressionNode condition, StatementNode update, Token closeParenthesis, StatementNode body, TextRange range) {
        super(NodeType.FOR_LOOP_STATEMENT, range);
        this.openParenthesis = openParenthesis;
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.closeParenthesis = closeParenthesis;
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
            return  other.openParenthesis.equals(openParenthesis) &&
                    other.init.equals(init) &&
                    Objects.equals(other.condition, condition) &&
                    other.update.equals(update) &&
                    other.closeParenthesis.equals(closeParenthesis) &&
                    other.body.equals(body) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}