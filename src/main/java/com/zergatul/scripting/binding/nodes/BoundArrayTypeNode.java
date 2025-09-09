package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ArrayTypeNode;
import com.zergatul.scripting.type.SArrayType;

import java.util.List;

public class BoundArrayTypeNode extends BoundTypeNode {

    public final ArrayTypeNode syntaxNode;
    public final BoundTypeNode underlying;

    public BoundArrayTypeNode(ArrayTypeNode node, BoundTypeNode underlying) {
        this(node, underlying, node.getRange());
    }

    public BoundArrayTypeNode(ArrayTypeNode node, BoundTypeNode underlying, TextRange range) {
        super(BoundNodeType.ARRAY_TYPE, new SArrayType(underlying.type), range);
        this.syntaxNode = node;
        this.underlying = underlying;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        underlying.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(underlying);
    }
}