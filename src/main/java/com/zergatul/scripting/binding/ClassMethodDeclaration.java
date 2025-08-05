package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundParameterListNode;
import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.symbols.SymbolRef;

public class ClassMethodDeclaration extends Declaration {

    private final boolean isAsync;
    private final BoundTypeNode typeNode;
    private final BoundParameterListNode parameters;
    private final boolean hasError;

    public ClassMethodDeclaration(String name, SymbolRef symbolRef, boolean isAsync, BoundTypeNode typeNode, BoundParameterListNode parameters, boolean hasError) {
        super(name, symbolRef);
        this.isAsync = isAsync;
        this.typeNode = typeNode;
        this.parameters = parameters;
        this.hasError = hasError;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public BoundTypeNode getTypeNode() {
        return typeNode;
    }

    public BoundParameterListNode getParameters() {
        return parameters;
    }
}