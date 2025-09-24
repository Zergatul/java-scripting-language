package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class IfStatementNode extends StatementNode {

    public final Token ifToken;
    public final Token openParen;
    public final ExpressionNode condition;
    public final Token closeParen;
    public final StatementNode thenStatement;
    public final Token elseToken;
    public final StatementNode elseStatement;

    public IfStatementNode(
            Token ifToken,
            Token openParen,
            ExpressionNode condition,
            Token closeParen,
            StatementNode thenStatement,
            Token elseToken,
            StatementNode elseStatement,
            TextRange range
    ) {
        super(ParserNodeType.IF_STATEMENT, range);
        this.ifToken = ifToken;
        this.openParen = openParen;
        this.condition = condition;
        this.closeParen = closeParen;
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
}