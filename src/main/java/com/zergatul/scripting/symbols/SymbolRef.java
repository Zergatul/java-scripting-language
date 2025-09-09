package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;

import java.util.ArrayList;
import java.util.List;

public abstract class SymbolRef {

    private final List<BoundNameExpressionNode> references = new ArrayList<>();

    public final void addReference(BoundNameExpressionNode name) {
        references.add(name);
    }

    public final List<BoundNameExpressionNode> getReferences() {
        return references;
    }

    public Function asFunction() {
        return (Function) get();
    }

    public Variable asVariable() {
        return (Variable) get();
    }

    public LocalVariable asLocalVariable() {
        return (LocalVariable) get();
    }

    public DeclaredStaticVariable asStaticVariable() {
        return (DeclaredStaticVariable) get();
    }

    public ClassSymbol asClass() {
        return (ClassSymbol) get();
    }

    public LocalVariable asLocalVariableExpanded() {
        Symbol symbol = get();
        if (symbol instanceof LocalVariable local) {
            return local;
        }
        if (symbol instanceof LiftedVariable lifted) {
            return lifted.getUnderlying();
        }
        throw new InternalException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SymbolRef other) {
            if (other.references.size() != references.size()) {
                return false;
            }
            for (int i = 0; i < references.size(); i++) {
                BoundNameExpressionNode name1 = other.references.get(i);
                BoundNameExpressionNode name2 = references.get(i);
                if (!name1.value.equals(name2.value)) {
                    return false;
                }
                if (!name1.type.equals(name2.type)) {
                    return false;
                }
                if (!name1.getRange().equals(name2.getRange())) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public abstract Symbol get();
    public abstract void set(Symbol symbol);
}