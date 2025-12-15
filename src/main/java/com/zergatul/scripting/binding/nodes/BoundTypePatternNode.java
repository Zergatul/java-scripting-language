package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.TypePatternNode;

import java.util.List;

public class BoundTypePatternNode extends BoundPatternNode {

    public final TypePatternNode syntaxNode;
    public final BoundTypeNode typeNode;

    public BoundTypePatternNode(TypePatternNode node, BoundTypeNode typeNode, TextRange range) {
        super(BoundNodeType.TYPE_PATTERN, range);
        this.syntaxNode = node;
        this.typeNode = typeNode;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        typeNode.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(typeNode);
    }
}