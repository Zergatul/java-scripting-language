package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.type.SString;

import java.util.List;

public class BoundStringLiteralExpressionNode extends BoundExpressionNode {

    public final String value;

    public BoundStringLiteralExpressionNode(String value, TextRange range) {
        super(NodeType.STRING_LITERAL, SString.instance, range);
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
}