package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.operation.UnaryOperation;

public class BoundPostfixStatementNode extends BoundStatementNode {

    public final BoundExpressionNode expression;
    public final UnaryOperation operation;

    public BoundPostfixStatementNode(NodeType nodeType, BoundExpressionNode expression, UnaryOperation operation, TextRange range) {
        super(nodeType, range);
        this.expression = expression;
        this.operation = operation;
    }
}