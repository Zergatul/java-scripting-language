package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundParameterListNode;
import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.symbols.SymbolRef;

public class ClassMethodDeclaration extends Declaration {

    private final BoundTypeNode typeNode;
    private final BoundParameterListNode parameters;
    private final boolean hasError;

    public ClassMethodDeclaration(String name, SymbolRef symbolRef, BoundTypeNode typeNode, BoundParameterListNode parameters, boolean hasError) {
        super(name, symbolRef);
        this.typeNode = typeNode;
        this.parameters = parameters;
        this.hasError = hasError;
    }

    public BoundTypeNode getTypeNode() {
        return typeNode;
    }

    public BoundParameterListNode getParameters() {
        return parameters;
    }
}