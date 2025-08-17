package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SInt;

import java.util.List;

public class BoundIntegerLiteralExpressionNode extends BoundExpressionNode {

    public final int value;

    public BoundIntegerLiteralExpressionNode(int value) {
        this(value, null);
    }

    public BoundIntegerLiteralExpressionNode(int value, TextRange range) {
        super(NodeType.INTEGER_LITERAL, SInt.instance, range);
        this.value = value;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public BoundNode accept(BinderTreeRewriter rewriter) {
        return rewriter.visit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundIntegerLiteralExpressionNode other) {
            return other.value == value && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}