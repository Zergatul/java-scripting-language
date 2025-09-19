package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.type.SInt64;

import java.util.List;

public class BoundInteger64LiteralExpressionNode extends BoundExpressionNode {

    public final long value;

    public BoundInteger64LiteralExpressionNode(long value) {
        this(value, null);
    }

    public BoundInteger64LiteralExpressionNode(long value, TextRange range) {
        super(NodeType.INTEGER64_LITERAL, SInt64.instance, range);
        this.value = value;
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
        if (obj instanceof BoundInteger64LiteralExpressionNode other) {
            return other.value == value && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}