package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class BoundWhileLoopStatementNode extends BoundStatementNode {

    public final BoundExpressionNode condition;
    public final BoundStatementNode body;

    public BoundWhileLoopStatementNode(BoundExpressionNode condition, BoundStatementNode body, TextRange range) {
        super(NodeType.WHILE_LOOP_STATEMENT, range);
        this.condition = condition;
        this.body = body;
    }
}