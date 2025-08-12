package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.symbols.MutableSymbolRef;
import com.zergatul.scripting.symbols.Symbol;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundNameExpressionNode extends BoundExpressionNode {

    public final String value;
    public final SymbolRef symbolRef;

    public BoundNameExpressionNode(Symbol symbol) {
        this(symbol, null);
    }

    public BoundNameExpressionNode(Symbol symbol, TextRange range) {
        this(symbol, symbol.getType(), symbol.getName(), range);
    }

    public BoundNameExpressionNode(Symbol symbol, SType type, String value, TextRange range) {
        this(new MutableSymbolRef(symbol), type, value, range);
    }

    public BoundNameExpressionNode(SymbolRef symbolRef) {
        this(symbolRef, null);
    }

    public BoundNameExpressionNode(SymbolRef symbolRef, TextRange range) {
        this(symbolRef, symbolRef.get().getType(), symbolRef.get().getName(), range);
    }

    public BoundNameExpressionNode(SymbolRef symbolRef, SType type, String value, TextRange range) {
        super(NodeType.NAME_EXPRESSION, type, range);
        this.symbolRef = symbolRef;
        this.value = value;

        if (symbolRef != null) {
            symbolRef.addReference(this);
        }
    }

    public Symbol getSymbol() {
        return symbolRef.get();
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public boolean canSet() {
        return getSymbol() != null && getSymbol().canSet();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundNameExpressionNode other) {
            return other.value.equals(value) && other.symbolRef.equals(symbolRef) && equals(other, this);
        } else {
            return false;
        }
    }
}