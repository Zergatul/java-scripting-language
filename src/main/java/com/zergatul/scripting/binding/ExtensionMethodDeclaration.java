package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundParameterListNode;
import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.MethodReference;

public class ExtensionMethodDeclaration extends ClassMethodDeclaration {

    private final MethodReference method;

    public ExtensionMethodDeclaration(
            String name,
            SymbolRef symbolRef,
            boolean isAsync,
            BoundTypeNode typeNode,
            BoundParameterListNode parameters,
            MethodReference method,
            boolean hasError
    ) {
        super(name, symbolRef, isAsync, typeNode, parameters, hasError);
        this.method = method;
    }

    public MethodReference getMethodReference() {
        return method;
    }
}