package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundAwaitExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode expression;

    public BoundAwaitExpressionNode(BoundExpressionNode expression, SType type, TextRange range) {
        super(NodeType.AWAIT_EXPRESSION, type, range);
        this.expression = expression;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression);
    }
}
