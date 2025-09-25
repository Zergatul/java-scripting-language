package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.PredefinedTypeNode;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundPredefinedTypeNode extends BoundTypeNode {

    public final PredefinedTypeNode syntaxNode;

    public BoundPredefinedTypeNode(PredefinedTypeNode node, SType type) {
        this(node, type, node.getRange());
    }

    public BoundPredefinedTypeNode(PredefinedTypeNode node, SType type, TextRange range) {
        super(BoundNodeType.PREDEFINED_TYPE, type, range);
        this.syntaxNode = node;
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