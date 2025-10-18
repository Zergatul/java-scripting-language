package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.NameExpressionNode;
import com.zergatul.scripting.symbols.ImmutableSymbolRef;
import com.zergatul.scripting.symbols.MutableSymbolRef;
import com.zergatul.scripting.symbols.Symbol;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.SType;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BoundNameExpressionNode extends BoundExpressionNode {

    public final NameExpressionNode syntaxNode;
    public final String value;
    public final SymbolRef symbolRef;

    public BoundNameExpressionNode(Symbol symbol) {
        this(new ImmutableSymbolRef(symbol));
    }

    public BoundNameExpressionNode(SymbolRef symbolRef) {
        this(SyntaxFactory.missingNameExpression(), symbolRef, symbolRef.get().getType(), symbolRef.get().getName(), TextRange.MISSING);
    }

    public BoundNameExpressionNode(NameExpressionNode node, Symbol symbol, SType type) {
        this(node, new MutableSymbolRef(symbol), type, node.value, node.getRange());
    }

    public BoundNameExpressionNode(NameExpressionNode node, SymbolRef symbolRef) {
        this(node, symbolRef, symbolRef.get().getType());
    }

    public BoundNameExpressionNode(NameExpressionNode node, SymbolRef symbolRef, SType type) {
        this(node, symbolRef, type, node.value, node.getRange());
    }

    public BoundNameExpressionNode(NameExpressionNode node, SymbolRef symbolRef, SType type, String value, TextRange range) {
        super(BoundNodeType.NAME_EXPRESSION, type, range);
        this.syntaxNode = node;
        this.symbolRef = symbolRef;
        this.value = value;

        symbolRef.addReference(this);
    }

    @Nullable
    public Symbol getSymbol() {
        return symbolRef.get();
    }

    public Symbol getSymbolOrThrow() {
        Symbol symbol = getSymbol();
        if (symbol == null) {
            throw new InternalException();
        }
        return symbol;
    }

    public SType getType() {
        return getSymbolOrThrow().getType();
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
}