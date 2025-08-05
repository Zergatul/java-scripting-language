package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundParameterListNode;
import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.SFunction;
import com.zergatul.scripting.type.SType;

public class FunctionDeclaration extends Declaration {

    private final boolean isAsync;
    private final BoundTypeNode returnTypeNode;
    private final BoundParameterListNode parameters;
    private final SFunction functionType;
    private final boolean hasError;

    public FunctionDeclaration(
            String name,
            SymbolRef symbolRef,
            boolean isAsync,
            BoundTypeNode returnTypeNode,
            BoundParameterListNode parameters,
            SFunction functionType,
            boolean hasError
    ) {
        super(name, symbolRef);
        this.isAsync = isAsync;
        this.returnTypeNode = returnTypeNode;
        this.parameters = parameters;
        this.functionType = functionType;
        this.hasError = hasError;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public boolean hasError() {
        return hasError;
    }

    public BoundParameterListNode getParameters() {
        return parameters;
    }

    public BoundTypeNode getReturnTypeNode() {
        return returnTypeNode;
    }

    public SType getReturnType() {
        return returnTypeNode.type;
    }
}
