package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundParameterListNode;
import com.zergatul.scripting.symbols.SymbolRef;

public class ClassConstructorDeclaration extends NamedDeclaration {

    private final BoundParameterListNode parameters;
    private final boolean hasError;

    public ClassConstructorDeclaration(SymbolRef symbolRef, BoundParameterListNode parameters, boolean hasError) {
        super("", symbolRef);
        this.parameters = parameters;
        this.hasError = hasError;
    }

    public BoundParameterListNode getParameters() {
        return parameters;
    }
}