package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SInt;

import java.util.List;

public class BoundIntegerLiteralExpressionNode extends BoundExpressionNode {

    public final int value;

    public BoundIntegerLiteralExpressionNode(int value, TextRange range) {
        super(NodeType.INTEGER_LITERAL, SInt.instance, range);
        this.value = value;
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}