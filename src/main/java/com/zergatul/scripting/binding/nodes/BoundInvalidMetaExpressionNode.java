package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SUnknown;

import java.util.List;

public class BoundInvalidMetaExpressionNode extends BoundExpressionNode {

    public BoundInvalidMetaExpressionNode(TextRange range) {
        super(NodeType.META_INVALID_EXPRESSION, SUnknown.instance, range);
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}