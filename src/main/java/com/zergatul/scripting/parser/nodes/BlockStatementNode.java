package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.ArrayList;
import java.util.List;

public class BlockStatementNode extends StatementNode {

    public final Token openBrace;
    public final List<StatementNode> statements;
    public final Token closeBrace;

    public BlockStatementNode(Token openBrace, List<StatementNode> statements, Token closeBrace) {
        super(ParserNodeType.BLOCK_STATEMENT, TextRange.combine(openBrace, closeBrace));
        this.openBrace = openBrace;
        this.statements = statements;
        this.closeBrace = closeBrace;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        for (StatementNode statement : statements) {
            statement.accept(visitor);
        }
    }

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> nodes = new ArrayList<>();
        nodes.add(openBrace);
        nodes.addAll(statements);
        nodes.add(closeBrace);
        return nodes;
    }
}