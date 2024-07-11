package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundReturnStatementNode extends BoundStatementNode {

    public final BoundExpressionNode expression;

    public BoundReturnStatementNode(BoundExpressionNode expression, TextRange range) {
        super(NodeType.RETURN_STATEMENT, range);
        this.expression = expression;
    }

    @Override
    public boolean isAsync() {
        return expression != null && expression.isAsync();
    }

    @Override
    public List<BoundNode> getChildren() {
        return expression == null ? List.of() : List.of(expression);
    }
}