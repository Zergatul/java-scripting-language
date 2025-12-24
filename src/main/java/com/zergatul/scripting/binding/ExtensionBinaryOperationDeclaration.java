package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundParameterListNode;
import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.UnaryOperation;

public class ExtensionBinaryOperationDeclaration {

    private final BoundTypeNode returnTypeNode;
    private final BoundParameterListNode parameters;
    private final SymbolRef symbolRef;
    private final BinaryOperation operation;
    private final boolean hasError;

    public ExtensionBinaryOperationDeclaration(
            BoundTypeNode returnTypeNode,
            BoundParameterListNode parameters,
            SymbolRef symbolRef,
            BinaryOperation operation,
            boolean hasError
    ) {
        this.returnTypeNode = returnTypeNode;
        this.parameters = parameters;
        this.symbolRef = symbolRef;
        this.operation = operation;
        this.hasError = hasError;
    }

    public BinaryOperation getOperation() {
        return operation;
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