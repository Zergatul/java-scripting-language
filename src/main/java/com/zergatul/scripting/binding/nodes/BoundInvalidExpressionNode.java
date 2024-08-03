package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SUnknown;

import java.util.List;

public class BoundInvalidExpressionNode extends BoundExpressionNode {

    public BoundInvalidExpressionNode(TextRange range) {
        super(NodeType.INVALID_EXPRESSION, SUnknown.instance, range);
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundInvalidExpressionNode other) {
            return other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}