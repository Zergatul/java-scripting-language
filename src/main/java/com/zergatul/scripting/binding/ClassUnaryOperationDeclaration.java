package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundParameterListNode;
import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SType;

public class ClassUnaryOperationDeclaration {

    private final BoundTypeNode returnTypeNode;
    private final UnaryOperator operator;
    private final BoundParameterListNode parameters;
    private final SymbolRef symbolRef;
    private final MethodReference method;
    private final boolean hasError;

    public ClassUnaryOperationDeclaration(
            BoundTypeNode returnTypeNode,
            UnaryOperator operator,
            BoundParameterListNode parameters,
            SymbolRef symbolRef,
            MethodReference method,
            boolean hasError
    ) {
        this.returnTypeNode = returnTypeNode;
        this.operator = operator;
        this.parameters = parameters;
        this.symbolRef = symbolRef;
        this.method = method;
        this.hasError = hasError;
    }

    public UnaryOperator getOperator() {
        return operator;
    }

    public MethodReference getMethodRef() {
        return method;
    }

    public SymbolRef getSymbolRef() {
        return symbolRef;
    }

    public SType getReturnType() {
        return returnTypeNode.type;
    }

    public BoundTypeNode getReturnTypeNode() {
        return returnTypeNode;
    }

    public BoundParameterListNode getParameters() {
        return parameters;
    }

    public boolean hasError() {
        return hasError;
    }
}