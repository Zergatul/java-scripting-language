package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.TypeAliasNode;
import com.zergatul.scripting.type.SAliasType;

import java.util.List;

public class BoundTypeAliasNode extends BoundCompilationUnitMemberNode {

    public final TypeAliasNode syntaxNode;
    public final BoundSymbolNode name;
    public final BoundTypeNode typeNode;

    public BoundTypeAliasNode(TypeAliasNode node, BoundSymbolNode name, BoundTypeNode typeNode) {
        super(BoundNodeType.TYPE_ALIAS, node.getRange());
        this.syntaxNode = node;
        this.name = name;
        this.typeNode = typeNode;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        name.accept(visitor);
        typeNode.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(name, typeNode);
    }

    public SAliasType getAliasType() {
        return (SAliasType) name.symbolRef.get().getType();
    }
}