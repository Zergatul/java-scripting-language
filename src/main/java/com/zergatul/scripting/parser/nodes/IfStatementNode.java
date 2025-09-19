package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.Objects;

public class IfStatementNode extends StatementNode {

    public final Token ifToken;
    public final Token openParen;
    public final Token closeParen;
    public final ExpressionNode condition;
    public final StatementNode thenStatement;
    public final Token elseToken;
    public final StatementNode elseStatement;

    public IfStatementNode(
            Token ifToken,
            Token openParen,
            Token closeParen,
            ExpressionNode condition,
            StatementNode thenStatement,
            Token elseToken,
            StatementNode elseStatement,
            TextRange range
    ) {
        super(NodeType.IF_STATEMENT, range);
        this.ifToken = ifToken;
        this.openParen = openParen;
        this.closeParen = closeParen;
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseToken = elseToken;
        this.elseStatement = elseStatement;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        condition.accept(visitor);
        thenStatement.accept(visitor);
        if (elseStatement != null) {
            elseStatement.accept(visitor);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IfStatementNode other) {
            return  other.ifToken.equals(ifToken) &&
                    other.openParen.equals(openParen) &&
                    other.condition.equals(condition) &&
                    other.closeParen.equals(closeParen) &&
                    other.thenStatement.equals(thenStatement) &&
                    Objects.equals(other.elseToken, elseToken) &&
                    Objects.equals(other.elseStatement, elseStatement) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}