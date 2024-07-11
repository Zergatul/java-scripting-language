package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundExpressionStatementNode extends BoundStatementNode {

    public final BoundExpressionNode expression;

    public BoundExpressionStatementNode(BoundExpressionNode expression, TextRange range) {
        super(NodeType.EXPRESSION_STATEMENT, range);
        this.expression = expression;
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