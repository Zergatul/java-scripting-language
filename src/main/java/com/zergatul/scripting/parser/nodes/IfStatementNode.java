package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IfStatementNode extends StatementNode {

    public final Token ifToken;
    public final Token openParen;
    public final ExpressionNode condition;
    public final Token closeParen;
    public final StatementNode thenStatement;
    @Nullable
    public final Token elseToken;
    @Nullable
    public final StatementNode elseStatement;

    public IfStatementNode(
            Token ifToken,
            Token openParen,
            ExpressionNode condition,
            Token closeParen,
            StatementNode thenStatement,
            @Nullable Token elseToken,
            @Nullable StatementNode elseStatement,
            TextRange range
    ) {
        super(ParserNodeType.IF_STATEMENT, range);

        assert (elseToken != null) == (elseStatement != null);

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

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> nodes = new ArrayList<>();
        nodes.add(ifToken);
        nodes.add(openParen);
        nodes.add(condition);
        nodes.add(closeParen);
        nodes.add(thenStatement);
        if (elseToken != null) {
            nodes.add(elseToken);
        }
        if (elseStatement != null) {
            nodes.add(elseStatement);
        }
        return nodes;
    }
}