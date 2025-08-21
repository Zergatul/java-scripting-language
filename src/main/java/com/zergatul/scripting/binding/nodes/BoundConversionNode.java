package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.ConversionType;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.operation.CastOperation;

import java.util.List;

public class BoundConversionNode extends BoundExpressionNode {

    public final BoundExpressionNode expression;
    public final ConversionType conversionType;
    public final CastOperation operation;

    public BoundConversionNode(BoundExpressionNode expression, ConversionType conversionType, SType type, TextRange range) {
        this(expression, conversionType, null, type, range);
    }

    public BoundConversionNode(BoundExpressionNode expression, ConversionType conversionType, CastOperation operation, SType type, TextRange range) {
        super(NodeType.CONVERSION, type, range);
        this.expression = expression;
        this.conversionType = conversionType;
        this.operation = operation;
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
    public List<BoundNode> getChildren() {
        return List.of(expression);
    }
}