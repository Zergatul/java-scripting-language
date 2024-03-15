package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.PropertyReference;

public class BoundPropertyAccessExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode callee;
    public final String name;
    public final PropertyReference property;

    public BoundPropertyAccessExpressionNode(BoundExpressionNode callee, String name, PropertyReference property, TextRange range) {
        super(NodeType.PROPERTY_ACCESS_EXPRESSION, property.getType(), range);
        this.callee = callee;
        this.name = name;
        this.property = property;
    }
}