package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundUnaryExpressionNode extends BoundExpressionNode {

    public final BoundUnaryOperatorNode operator;
    public final BoundExpressionNode operand;

    public BoundUnaryExpressionNode(BoundUnaryOperatorNode operator, BoundExpressionNode operand) {
        this(operator, operand, null);
    }

    public BoundUnaryExpressionNode(BoundUnaryOperatorNode operator, BoundExpressionNode operand, TextRange range) {
        super(NodeType.UNARY_EXPRESSION, operator.operation.type, range);
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        operator.accept(visitor);
        operand.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(operator, operand);
    }
}