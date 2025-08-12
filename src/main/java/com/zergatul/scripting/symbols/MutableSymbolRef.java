package com.zergatul.scripting.symbols;

public class MutableSymbolRef extends SymbolRef {

    private Symbol symbol;

    public MutableSymbolRef(Symbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public Symbol get() {
        return symbol;
    }

    @Override
    public void set(Symbol symbol) {
        this.symbol = symbol;
    }
}