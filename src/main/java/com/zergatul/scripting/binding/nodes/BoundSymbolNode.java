package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.symbols.SymbolRef;

import java.util.List;

public class BoundSymbolNode extends BoundNode {

    public final ValueToken token;
    public final SymbolRef symbolRef;

    public BoundSymbolNode(ValueToken token, SymbolRef symbolRef) {
        super(BoundNodeType.SYMBOL, token.getRange());
        this.token = token;
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
}