package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class WhileLoopStatementNode extends StatementNode {

    public final Token keyword;
    public final Token openParen;
    public final ExpressionNode condition;
    public final Token closeParen;
    public final StatementNode body;

    public WhileLoopStatementNode(Token keyword, Token openParen, ExpressionNode condition, Token closeParen, StatementNode body, TextRange range) {
        super(NodeType.WHILE_LOOP_STATEMENT, range);
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WhileLoopStatementNode other) {
            return  other.keyword.equals(keyword) &&
                    other.openParen.equals(openParen) &&
                    other.condition.equals(condition) &&
                    other.closeParen.equals(closeParen) &&
                    other.body.equals(body) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}