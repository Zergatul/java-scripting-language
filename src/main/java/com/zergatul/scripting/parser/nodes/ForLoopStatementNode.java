package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

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
}