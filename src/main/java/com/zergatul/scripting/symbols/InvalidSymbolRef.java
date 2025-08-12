package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;

public class InvalidSymbolRef extends SymbolRef {
    @Override
    public Symbol get() {
        return null;
    }

    @Override
    public void set(Symbol symbol) {
        throw new InternalException();
    }
}