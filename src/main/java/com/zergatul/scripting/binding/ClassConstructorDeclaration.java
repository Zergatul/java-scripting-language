package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundParameterListNode;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.ConstructorReference;

public class ClassConstructorDeclaration extends NamedDeclaration {

    private final BoundParameterListNode parameters;
    private final ConstructorReference constructor;
    private final boolean hasError;

    public ClassConstructorDeclaration(
            SymbolRef symbolRef,
            BoundParameterListNode parameters,
            ConstructorReference constructor,
            boolean hasError
    ) {
        super("", symbolRef);
        this.parameters = parameters;
        this.constructor = constructor;
        this.hasError = hasError;
    }

    public BoundParameterListNode getParameters() {
        return parameters;
    }

    public ConstructorReference getConstructorReference() {
        return constructor;
    }
}