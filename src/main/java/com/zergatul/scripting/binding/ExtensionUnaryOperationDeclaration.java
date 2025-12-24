package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundParameterListNode;
import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.operation.UnaryOperation;

public class ExtensionUnaryOperationDeclaration {

    private final BoundTypeNode returnTypeNode;
    private final BoundParameterListNode parameters;
    private final SymbolRef symbolRef;
    private final UnaryOperation operation;
    private final boolean hasError;

    public ExtensionUnaryOperationDeclaration(
            BoundTypeNode returnTypeNode,
            BoundParameterListNode parameters,
            SymbolRef symbolRef,
            UnaryOperation operation,
            boolean hasError
    ) {
        this.returnTypeNode = returnTypeNode;
        this.parameters = parameters;
        this.symbolRef = symbolRef;
        this.operation = operation;
        this.hasError = hasError;
    }

    public UnaryOperation getOperation() {
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