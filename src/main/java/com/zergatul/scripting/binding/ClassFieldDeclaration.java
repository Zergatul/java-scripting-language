package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.PropertyReference;

public class ClassFieldDeclaration extends NamedDeclaration {

    private final BoundTypeNode typeNode;
    private final PropertyReference property;
    private final boolean hasError;

    public ClassFieldDeclaration(String name, SymbolRef symbolRef, BoundTypeNode typeNode, PropertyReference property, boolean hasError) {
        super(name, symbolRef);
        this.typeNode = typeNode;
        this.property = property;
        this.hasError = hasError;
    }

    public BoundTypeNode getTypeNode() {
        return typeNode;
    }

    public PropertyReference getPropertyReference() {
        return property;
    }
}