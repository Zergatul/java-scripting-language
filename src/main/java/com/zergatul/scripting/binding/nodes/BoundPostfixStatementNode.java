package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.operation.PostfixOperation;

import java.util.List;

public class BoundPostfixStatementNode extends BoundStatementNode {

    public final BoundExpressionNode expression;
    public final PostfixOperation operation;

    public BoundPostfixStatementNode(NodeType nodeType, BoundExpressionNode expression, PostfixOperation operation, TextRange range) {
        super(nodeType, range);
        this.expression = expression;
        this.operation = operation;
    }

    @Override
    public boolean isAsync() {
        return expression.isAsync();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression);
    }
}