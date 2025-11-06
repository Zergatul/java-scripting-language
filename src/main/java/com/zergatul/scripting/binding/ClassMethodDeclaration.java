package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundParameterListNode;
import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.MethodReference;

public class ClassMethodDeclaration extends NamedDeclaration {

    private final boolean isAsync;
    private final BoundTypeNode typeNode;
    private final BoundParameterListNode parameters;
    private final MethodReference method;
    private final boolean hasError;

    public ClassMethodDeclaration(
            String name,
            SymbolRef symbolRef,
            boolean isAsync,
            BoundTypeNode typeNode,
            BoundParameterListNode parameters,
            MethodReference method,
            boolean hasError
    ) {
        super(name, symbolRef);
        this.isAsync = isAsync;
        this.typeNode = typeNode;
        this.parameters = parameters;
        this.method = method;
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

    public MethodReference getMethodReference() {
        return method;
    }
}