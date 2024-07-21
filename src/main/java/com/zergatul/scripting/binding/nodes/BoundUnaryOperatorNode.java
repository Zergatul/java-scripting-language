package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.operation.UnaryOperation;

import java.util.List;

public class BoundUnaryOperatorNode extends BoundNode {

    public final UnaryOperation operation;

    public BoundUnaryOperatorNode(UnaryOperation operation) {
        this(operation, null);
    }

    public BoundUnaryOperatorNode(UnaryOperation operation, TextRange range) {
        super(NodeType.UNARY_OPERATOR, range);
        this.operation = operation;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}