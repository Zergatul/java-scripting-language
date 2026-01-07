package com.zergatul.scripting.binding;

import com.zergatul.scripting.symbols.FunctionGroup;
import com.zergatul.scripting.symbols.SymbolRef;

import java.util.ArrayList;
import java.util.List;

public class FunctionGroupDeclaration extends NamedDeclaration {

    private final List<FunctionDeclaration> functions;
    private final boolean hasError;

    public FunctionGroupDeclaration(String name, SymbolRef symbolRef, boolean hasError) {
        super(name, symbolRef);
        this.functions = new ArrayList<>();
        this.hasError = hasError;
    }

    public void addFunction(FunctionDeclaration declaration) {
        functions.add(declaration);
        getFunctionGroup().addFunction(declaration.getSymbolRef().asFunction());
    }

    public FunctionGroup getFunctionGroup() {
        return (FunctionGroup) getSymbolRef().get();
    }

    public boolean hasError() {
        return hasError;
    }
}