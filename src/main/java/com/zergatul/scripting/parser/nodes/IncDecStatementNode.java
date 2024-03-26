package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class IncDecStatementNode extends StatementNode {

    public final ExpressionNode expression;

    public IncDecStatementNode(NodeType nodeType, ExpressionNode expression, TextRange range) {
        super(nodeType, range);
        this.expression = expression;
    }
}