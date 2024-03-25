package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class BoundReturnStatementNode extends BoundStatementNode {

    public final BoundExpressionNode expression;

    public BoundReturnStatementNode(BoundExpressionNode expression, TextRange range) {
        super(NodeType.RETURN_STATEMENT, range);
        this.expression = expression;
    }
}