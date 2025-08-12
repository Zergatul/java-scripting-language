package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;

public class ForwardSymbolRef extends SymbolRef {

    private Symbol symbol;

    @Override
    public Symbol get() {
        if (symbol == null) {
            throw new InternalException();
        }
        return symbol;
    }

    @Override
    public void set(Symbol symbol) {
        if (this.symbol != null) {
            throw new InternalException();
        }
        this.symbol = symbol;
    }
}