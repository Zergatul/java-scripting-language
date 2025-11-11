package com.zergatul.scripting.binding;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.nodes.BoundTypeAliasNode;
import com.zergatul.scripting.symbols.*;
import org.jspecify.annotations.Nullable;

public class TypeAliasDeclaration extends NamedDeclaration {

    @Nullable
    private BoundTypeAliasNode bound;

    public TypeAliasDeclaration(String name, SymbolRef symbolRef) {
        super(name, symbolRef);
    }

    public TypeAliasSymbol getSymbol() {
        return (TypeAliasSymbol) getSymbolRef().get();
    }

    public BoundTypeAliasNode getBound() {
        if (bound == null) {
            throw new InternalException();
        }
        return bound;
    }

    public void setBound(BoundTypeAliasNode bound) {
        this.bound = bound;
    }
}