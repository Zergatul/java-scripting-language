package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.CustomTypeNode;
import com.zergatul.scripting.symbols.ClassSymbol;
import com.zergatul.scripting.symbols.SymbolRef;

import java.util.List;

public class BoundDeclaredClassTypeNode extends BoundTypeNode {

    public final CustomTypeNode syntaxNode;
    public final SymbolRef symbolRef;

    public BoundDeclaredClassTypeNode(CustomTypeNode node, SymbolRef symbolRef) {
        this(node, symbolRef, node.getRange());
    }

    public BoundDeclaredClassTypeNode(CustomTypeNode node, SymbolRef symbolRef, TextRange range) {
        super(BoundNodeType.DECLARED_CLASS_TYPE, symbolRef.get().getType(), range);
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

    public ClassSymbol getSymbol() {
        return symbolRef.asClass();
    }
}
