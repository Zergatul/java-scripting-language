package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserVisitor;

public class WhileLoopStatementNode extends StatementNode {

    public final ExpressionNode condition;
    public final StatementNode body;

    public WhileLoopStatementNode(ExpressionNode condition, StatementNode body, TextRange range) {
        super(NodeType.WHILE_LOOP_STATEMENT, range);
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void accept(ParserVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserVisitor visitor) {
        condition.accept(visitor);
        body.accept(visitor);
    }
}