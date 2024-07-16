package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.symbols.Symbol;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundNameExpressionNode extends BoundExpressionNode {

    public final String value;
    public Symbol symbol;

    public BoundNameExpressionNode(Symbol symbol, TextRange range) {
        this(symbol, symbol.getType(), symbol.getName(), range);
    }

    public BoundNameExpressionNode(Symbol symbol, SType type, String value, TextRange range) {
        super(NodeType.NAME_EXPRESSION, type, range);
        this.symbol = symbol;
        this.value = value;

        if (symbol != null) {
            symbol.addReference(this);
        }
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    public void overrideSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public boolean canSet() {
        return symbol != null && symbol.canSet();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}