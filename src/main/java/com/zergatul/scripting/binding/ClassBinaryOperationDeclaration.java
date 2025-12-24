package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundParameterListNode;
import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SType;

public class ClassBinaryOperationDeclaration {

    private final BoundTypeNode returnTypeNode;
    private final BinaryOperator operator;
    private final BoundParameterListNode parameters;
    private final SymbolRef symbolRef;
    private final MethodReference method;
    private final boolean hasError;

    public ClassBinaryOperationDeclaration(
            BoundTypeNode returnTypeNode,
            BinaryOperator operator,
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

    public BinaryOperator getOperator() {
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

    public SType getLeftType() {
        return parameters.parameters.getFirst().getType();
    }

    public SType getRightType() {
        return parameters.parameters.getLast().getType();
    }

    public BoundParameterListNode getParameters() {
        return parameters;
    }

    public boolean hasError() {
        return hasError;
    }
}