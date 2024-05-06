package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.SFloat;
import com.zergatul.scripting.type.SReference;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.SVoidType;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CompilerContext {

    private final CompilerContext root;
    private final CompilerContext parent;
    private final Map<String, Symbol> staticSymbols = new HashMap<>();
    private final Map<String, Symbol> localSymbols = new HashMap<>();
    private final boolean isFunctionRoot;
    private final SType returnType;
    private String className;
    private int stackIndex;
    private Consumer<MethodVisitor> breakConsumer;
    private Consumer<MethodVisitor> continueConsumer;
    private List<RefHolder> refVariables = new ArrayList<>();

    public CompilerContext() {
        this(1);
    }

    public CompilerContext(int initialStackIndex) {
        this(initialStackIndex, false, SVoidType.instance, null);
    }

    public CompilerContext(int initialStackIndex, boolean isFunctionRoot, SType returnType, CompilerContext parent) {
        this.root = parent == null ? this : parent.root;
        this.parent = parent;
        this.stackIndex = initialStackIndex;
        this.isFunctionRoot = isFunctionRoot;
        this.returnType = returnType;
    }

    public void addStaticVariable(StaticVariable variable) {
        if (hasSymbol(variable.getName())) {
            throw new InternalException();
        }

        staticSymbols.put(variable.getName(), variable);
    }

    public void addFunction(Function function) {
        if (hasSymbol(function.getName())) {
            throw new InternalException();
        }

        staticSymbols.put(function.getName(), function);
    }

    public LocalVariable addLocalVariable(String name, SType type) {
        if (name != null && hasSymbol(name)) {
            throw new InternalException();
        }

        LocalVariable variable = new LocalVariable(name, type, stackIndex);
        addLocalVariable(variable);
        if (type == SFloat.instance) {
            stackIndex += 2;
        } else {
            stackIndex += 1;
        }
        return variable;
    }

    public void addLocalVariable(LocalVariable variable) {
        if (variable.getName() != null && hasSymbol(variable.getName())) {
            throw new InternalException();
        }

        localSymbols.put(variable.getName(), variable);
    }

    public LocalVariable addLocalRefVariable(String name, SReference refType, SType underlying) {
        if (name == null || name.isEmpty()) {
            throw new InternalException();
        }

        LocalVariable variable = new LocalRefVariable(name, refType, underlying, stackIndex++);
        addLocalVariable(variable);
        return variable;
    }

    public LocalVariable createRefVariable(Variable variable) {
        LocalVariable holder = addLocalVariable(null, variable.getType().getReferenceType());
        refVariables.add(new RefHolder(holder, variable));
        return holder;
    }

    public List<RefHolder> releaseRefVariables() {
        return List.of(refVariables.toArray(RefHolder[]::new));
    }

    public CompilerContext createChild() {
        return new CompilerContext(stackIndex, false, returnType, this);
    }

    public CompilerContext createStaticFunction(SType returnType) {
        return new CompilerContext(0, true, returnType, this);
    }

    public CompilerContext createFunction(SType returnType) {
        return new CompilerContext(1, true, returnType, this);
    }

    public SType getReturnType() {
        return returnType;
    }

    public CompilerContext getParent() {
        return parent;
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
            if (context.isFunctionRoot) {
                break;
            }
            context = context.parent;
        }

        return null;
    }

    public boolean hasSymbol(String name) {
        return getSymbol(name) != null;
    }

    public void setBreak(Consumer<MethodVisitor> consumer) {
        breakConsumer = consumer;
    }

    public void setContinue(Consumer<MethodVisitor> consumer) {
        continueConsumer = consumer;
    }

    public boolean canBreak() {
        for (CompilerContext context = this; context != null; context = context.parent) {
            if (context.breakConsumer != null) {
                return true;
            }
        }
        return false;
    }

    public boolean canContinue() {
        for (CompilerContext context = this; context != null; context = context.parent) {
            if (context.continueConsumer != null) {
                return true;
            }
        }
        return false;
    }

    public void compileBreak(MethodVisitor visitor) {
        for (CompilerContext context = this; context != null; context = context.parent) {
            if (context.breakConsumer != null) {
                context.breakConsumer.accept(visitor);
                return;
            }
        }

        throw new InternalException(); // no loop
    }

    public void compileContinue(MethodVisitor visitor) {
        for (CompilerContext context = this; context != null; context = context.parent) {
            if (context.continueConsumer != null) {
                context.continueConsumer.accept(visitor);
                return;
            }
        }

        throw new InternalException(); // no loop
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return root.className;
    }
}