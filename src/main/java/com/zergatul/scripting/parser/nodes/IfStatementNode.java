package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.Objects;

public class IfStatementNode extends StatementNode {

    public final Token lParen;
    public final Token rParen;
    public final ExpressionNode condition;
    public final StatementNode thenStatement;
    public final StatementNode elseStatement;

    public IfStatementNode(Token lParen, Token rParen, ExpressionNode condition, StatementNode thenStatement, StatementNode elseStatement, TextRange range) {
        super(NodeType.IF_STATEMENT, range);
        this.lParen = lParen;
        this.rParen = rParen;
        this.condition = condition;
        this.thenStatement = thenStatement;
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
            return  other.condition.equals(condition) &&
                    other.thenStatement.equals(thenStatement) &&
                    Objects.equals(other.elseStatement, elseStatement) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}