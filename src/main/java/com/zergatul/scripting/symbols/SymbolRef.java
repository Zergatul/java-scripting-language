package com.zergatul.scripting.symbols;

import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class SymbolRef {

    private final List<BoundNameExpressionNode> references = new ArrayList<>();

    public final void addReference(BoundNameExpressionNode name) {
        references.add(name);
    }

    public final List<BoundNameExpressionNode> getReferences() {
        return references;
    }

    public Variable asVariable() {
        return (Variable) get();
    }

    public LocalVariable asLocalVariable() {
        return (LocalVariable) get();
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