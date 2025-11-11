package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.CustomTypeNode;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.symbols.TypeAliasSymbol;
import com.zergatul.scripting.type.SAliasType;

import java.util.List;

public class BoundAliasedTypeNode extends BoundTypeNode {

    public final CustomTypeNode syntaxNode;
    public final SymbolRef symbolRef;

    public BoundAliasedTypeNode(CustomTypeNode node, SymbolRef symbolRef) {
        this(node, symbolRef, node.getRange());
    }

    public BoundAliasedTypeNode(CustomTypeNode node, SymbolRef symbolRef, TextRange range) {
        super(BoundNodeType.ALIASED_TYPE, ((SAliasType) symbolRef.get().getType()).getFinalType(), range);
        this.syntaxNode = node;
        this.symbolRef = symbolRef;
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

    public TypeAliasSymbol getSymbol() {
        return (TypeAliasSymbol) symbolRef.get();
    }
}