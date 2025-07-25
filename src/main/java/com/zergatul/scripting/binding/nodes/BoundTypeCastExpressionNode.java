package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundTypeCastExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode expression;
    public final BoundTypeNode type;

    public BoundTypeCastExpressionNode(BoundExpressionNode expression, BoundTypeNode type, TextRange range) {
        super(NodeType.TYPE_CAST_EXPRESSION, type.type, range);
        this.expression = expression;
        this.type = type;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        expression.accept(visitor);
        type.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression, type);
    }
}