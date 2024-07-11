package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SStaticTypeReference;

import java.util.List;

public class BoundStaticReferenceExpression extends BoundExpressionNode {

    public BoundStaticReferenceExpression(SStaticTypeReference type, TextRange range) {
        super(NodeType.STATIC_REFERENCE, type, range);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}