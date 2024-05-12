package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SUnknown;

import java.util.List;

public class BoundInvalidExpressionNode extends BoundExpressionNode {

    public BoundInvalidExpressionNode(TextRange range) {
        super(NodeType.INVALID_EXPRESSION, SUnknown.instance, range);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}