package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class BoundForLoopStatementNode extends BoundStatementNode {

    public final BoundStatementNode init;
    public final BoundExpressionNode condition;
    public final BoundStatementNode update;
    public final BoundStatementNode body;

    public BoundForLoopStatementNode(BoundStatementNode init, BoundExpressionNode condition, BoundStatementNode update, BoundStatementNode body, TextRange range) {
        super(NodeType.FOR_LOOP_STATEMENT, range);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }
}