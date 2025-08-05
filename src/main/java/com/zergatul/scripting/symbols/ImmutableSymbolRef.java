package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;

public class ImmutableSymbolRef extends SymbolRef {

    private final Symbol symbol;

    public ImmutableSymbolRef(Symbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public Symbol get() {
        return symbol;
    }

    @Override
    public void set(Symbol symbol) {
        throw new InternalException();
    }
}