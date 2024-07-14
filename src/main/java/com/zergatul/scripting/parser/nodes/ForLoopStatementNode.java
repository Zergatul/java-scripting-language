package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserVisitor;

public class ForLoopStatementNode extends StatementNode {

    public final StatementNode init;
    public final ExpressionNode condition;
    public final StatementNode update;
    public final StatementNode body;

    public ForLoopStatementNode(StatementNode init, ExpressionNode condition, StatementNode update, StatementNode body, TextRange range) {
        super(NodeType.FOR_LOOP_STATEMENT, range);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    @Override
    public void accept(ParserVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserVisitor visitor) {
        init.accept(visitor);
        if (condition != null) {
            condition.accept(visitor);
        }
        update.accept(visitor);
        body.accept(visitor);
    }
}