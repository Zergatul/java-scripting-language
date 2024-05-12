package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SFloat;

import java.util.List;

public class BoundFloatLiteralExpressionNode extends BoundExpressionNode {

    public final double value;

    public BoundFloatLiteralExpressionNode(double value, TextRange range) {
        super(NodeType.FLOAT_LITERAL, SFloat.instance, range);
        this.value = value;
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}