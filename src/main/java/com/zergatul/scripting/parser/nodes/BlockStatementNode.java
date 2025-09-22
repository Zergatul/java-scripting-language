package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class BlockStatementNode extends StatementNode {

    public final Token openBrace;
    public final List<StatementNode> statements;
    public final Token closeBrace;

    public BlockStatementNode(Token openBrace, List<StatementNode> statements, Token closeBrace, TextRange range) {
        super(ParserNodeType.BLOCK_STATEMENT, range);
        this.openBrace = openBrace;
        this.statements = statements;
        this.closeBrace = closeBrace;
    }

    public BlockStatementNode(List<StatementNode> statements, TextRange range) {
        super(ParserNodeType.BLOCK_STATEMENT, range);
        throw new InternalException();
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
}