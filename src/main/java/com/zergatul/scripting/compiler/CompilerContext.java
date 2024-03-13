package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.SFloatType;
import com.zergatul.scripting.type.SType;

import java.util.HashMap;
import java.util.Map;

public class CompilerContext {

    private final CompilerContext root;
    private final CompilerContext parent;
    private final Map<String, Symbol> staticSymbols = new HashMap<>();
    private final Map<String, Symbol> localSymbols = new HashMap<>();
    private int stackIndex;

    public CompilerContext() {
        this(1);
    }

    public CompilerContext(int initialStackIndex) {
        this(initialStackIndex, null);
    }

    public CompilerContext(int initialStackIndex, CompilerContext parent) {
        this.root = parent == null ? this : parent.root;
        this.parent = parent;
        this.stackIndex = initialStackIndex;
    }

    public void addStaticVariable(StaticVariable variable) {
        if (hasSymbol(variable.getName())) {
            throw new InternalException();
        }

        staticSymbols.put(variable.getName(), variable);
    }

    public LocalVariable addLocalVariable(String name, SType type) {
        if (hasSymbol(name)) {
            throw new InternalException();
        }

        LocalVariable variable = new LocalVariable(name, type, stackIndex);
        addLocalVariable(variable);
        if (type == SFloatType.instance) {
            stackIndex += 2;
        } else {
            stackIndex += 1;
        }
        return variable;
    }

    public void addLocalVariable(LocalVariable variable) {
        if (hasSymbol(variable.getName())) {
            throw new InternalException();
        }

        localSymbols.put(variable.getName(), variable);
    }

    public CompilerContext createChild() {
        return new CompilerContext(stackIndex, this);
    }

    public Symbol getSymbol(String name) {
        Symbol staticSymbol = root.staticSymbols.get(name);
        if (staticSymbol != null) {
            return staticSymbol;
        }

        for (CompilerContext context = this; context != null; ) {
            Symbol localSymbol = context.localSymbols.get(name);
            if (localSymbol != null) {
                return localSymbol;
            }
            context = context.parent;
        }

        return null;
    }

    public boolean hasSymbol(String name) {
        return getSymbol(name) != null;
    }
}