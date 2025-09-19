package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.ConversionInfo;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundConversionNode extends BoundExpressionNode {

    public final BoundExpressionNode expression;
    public final ConversionInfo conversionInfo;

    public BoundConversionNode(BoundExpressionNode expression, ConversionInfo conversionInfo, SType type, TextRange range) {
        super(NodeType.CONVERSION, type, range);
        this.expression = expression;
        this.conversionInfo = conversionInfo;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        expression.accept(visitor);
    }

    @Override
    public boolean isOpen() {
        return expression.isOpen();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression);
    }
}