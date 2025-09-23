package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class StatementsListNode extends StatementNode {

    public final List<StatementNode> statements;

    public StatementsListNode(List<StatementNode> statements, TextRange range) {
        super(ParserNodeType.STATEMENTS_LIST, range);
        this.statements = statements;
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