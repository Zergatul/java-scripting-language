package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;

public class DefinedSymbolHolder extends SymbolHolder {

    private final Symbol symbol;

    public DefinedSymbolHolder(Symbol symbol) {
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