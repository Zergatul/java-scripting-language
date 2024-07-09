package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;
import com.zergatul.scripting.type.SFloat;
import com.zergatul.scripting.type.SReference;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.SVoidType;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CompilerContext {

    private final CompilerContext root;
    private final CompilerContext parent;
    private final Map<String, Symbol> staticSymbols = new HashMap<>();
    private final Map<String, Variable> localSymbols = new HashMap<>();
    private final boolean isFunctionRoot;
    private final boolean isGenericFunction;
    private final SType returnType;
    private String className;
    private int stackIndex;
    private Consumer<MethodVisitor> breakConsumer;
    private Consumer<MethodVisitor> continueConsumer;
    private final List<RefHolder> refVariables = new ArrayList<>();
    private boolean isAsync;

    public CompilerContext() {
        this(1);
    }

    public CompilerContext(int initialStackIndex) {
        this(initialStackIndex, false, SVoidType.instance, null);
    }

    public CompilerContext(int initialStackIndex, boolean isFunctionRoot, SType returnType, CompilerContext parent) {
        this(initialStackIndex, isFunctionRoot, false, returnType, parent);
    }

    public CompilerContext(int initialStackIndex, boolean isFunctionRoot, boolean isGenericFunction, SType returnType, CompilerContext parent) {
        this.root = parent == null ? this : parent.root;
        this.parent = parent;
        this.stackIndex = initialStackIndex;
        this.isFunctionRoot = isFunctionRoot;
        this.isGenericFunction = isGenericFunction;
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

    public LocalVariable addLocalVariable(String name, SType type, TextRange definition) {
        if (name != null && hasSymbol(name)) {
            throw new InternalException();
        }

        LocalVariable variable = new LocalVariable(name, type, stackIndex, definition);
        addLocalVariable(variable);
        return variable;
    }

    public void addLocalVariable(Variable variable) {
        if (variable.getName() != null && hasSymbol(variable.getName())) {
            throw new InternalException();
        }

        if (variable instanceof LocalVariable local) {
            expandStackOnLocalVariable(local);
        }
        if (variable instanceof LiftedLocalVariable lifted) {
            Variable underlying = lifted.getUnderlyingVariable();
            if (underlying instanceof LocalVariable local) {
                expandStackOnLocalVariable(local);
            }
        }

        localSymbols.put(variable.getName(), variable);
    }

    public LocalVariable addLocalParameter(String name, SType type, TextRange definition) {
        if (name != null && hasSymbol(name)) {
            throw new InternalException();
        }

        LocalVariable variable = new LocalParameter(name, type, stackIndex, definition);
        addLocalVariable(variable);
        return variable;
    }

    public LocalVariable addLocalRefParameter(String name, SReference refType, SType underlying, TextRange definition) {
        if (name == null || name.isEmpty()) {
            throw new InternalException();
        }

        LocalVariable variable = new LocalRefParameter(name, refType, underlying, stackIndex++, definition);
        addLocalVariable(variable);
        return variable;
    }

    public boolean isGenericFunction() {
        return isGenericFunction;
    }

    public LocalVariable createRefVariable(Variable variable) {
        LocalVariable holder = addLocalVariable(null, variable.getType().getReferenceType(), null);
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
        return createFunction(returnType, false);
    }

    public CompilerContext createFunction(SType returnType, boolean generic) {
        return new CompilerContext(1, true, generic, returnType, this);
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

        List<CompilerContext> functions = List.of(); // function boundaries
        for (CompilerContext context = this; context != null; ) {
            Variable localSymbol = context.localSymbols.get(name);
            if (localSymbol != null) {
                if (functions.isEmpty()) {
                    return localSymbol;
                } else {
                    if (!(localSymbol instanceof LiftedLocalVariable)) {
                        LiftedLocalVariable lifted = new LiftedLocalVariable(localSymbol);
                        for (BoundNameExpressionNode nameExpression : localSymbol.getReferences()) {
                            nameExpression.overrideSymbol(lifted);
                        }
                        localSymbol = lifted;
                        context.localSymbols.put(name, localSymbol); // replace local variable with lifted
                    }

                    Variable prev = localSymbol;
                    for (int i = functions.size() - 1; i >= 0; i--) {
                        Variable current = new CapturedLocalVariable(prev);
                        functions.get(i).localSymbols.put(name, current);
                        prev = current;
                    }
                    return functions.get(0).localSymbols.get(name);
                }
            }
            if (context.isFunctionRoot) {
                if (functions.isEmpty()) {
                    functions = new ArrayList<>();
                }
                functions.add(context);
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

    public List<CapturedLocalVariable> getCaptured() {
        return localSymbols.values().stream()
                .filter(s -> s instanceof CapturedLocalVariable)
                .map(s -> (CapturedLocalVariable) s)
                .toList();
    }

    public void markAsync() {
        isAsync = true;
    }

    public boolean isAsync() {
        return isAsync;
    }

    private void expandStackOnLocalVariable(LocalVariable variable) {
        int stack = variable.getStackIndex() + getStackSize(variable.getType());
        if (stack > stackIndex) {
            stackIndex = stack;
        }
    }

    private int getStackSize(SType type) {
        return type == SFloat.instance ? 2 : 1;
    }
}