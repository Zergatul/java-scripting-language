package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.symbols.SymbolRef;

public class ClassFieldDeclaration extends NamedDeclaration {

    private final BoundTypeNode typeNode;
    private final boolean hasError;

    public ClassFieldDeclaration(String name, SymbolRef symbolRef, BoundTypeNode typeNode, boolean hasError) {
        super(name, symbolRef);
        this.typeNode = typeNode;
        this.hasError = hasError;
    }

    public BoundTypeNode getTypeNode() {
        return typeNode;
    }
}