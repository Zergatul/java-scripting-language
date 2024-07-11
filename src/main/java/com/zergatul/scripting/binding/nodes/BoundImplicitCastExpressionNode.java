package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.operation.CastOperation;
import com.zergatul.scripting.type.operation.UnaryOperation;

import java.util.List;

public class BoundImplicitCastExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode operand;
    public final CastOperation operation;

    public BoundImplicitCastExpressionNode(BoundExpressionNode operand, CastOperation operation, TextRange range) {
        super(NodeType.IMPLICIT_CAST, operand.type, range);
        this.operand = operand;
        this.operation = operation;
    }

    @Override
    public boolean isAsync() {
        return operand.isAsync();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(operand);
    }
}