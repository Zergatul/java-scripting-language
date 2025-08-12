package com.zergatul.scripting.binding;

import com.zergatul.scripting.symbols.SymbolRef;

public abstract class Declaration {

    private final String name;
    private final SymbolRef symbolRef;

    protected Declaration(String name, SymbolRef symbolRef) {
        this.name = name;
        this.symbolRef = symbolRef;
    }

    public String getName() {
        return name;
    }

    public SymbolRef getSymbolRef() {
        return symbolRef;
    }
}