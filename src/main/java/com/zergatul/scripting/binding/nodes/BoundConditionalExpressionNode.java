package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundConditionalExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode condition;
    public final BoundExpressionNode whenTrue;
    public final BoundExpressionNode whenFalse;

    public BoundConditionalExpressionNode(BoundExpressionNode condition, BoundExpressionNode whenTrue, BoundExpressionNode whenFalse, TextRange range) {
        super(NodeType.CONDITIONAL_EXPRESSION, whenTrue.type, range);
        this.condition = condition;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }

    @Override
    public boolean isAsync() {
        return condition.isAsync() || whenTrue.isAsync() || whenFalse.isAsync();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(condition, whenTrue, whenFalse);
    }
}