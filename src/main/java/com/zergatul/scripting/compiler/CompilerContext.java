package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;
import com.zergatul.scripting.binding.nodes.FunctionRootNode;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.SFloat;
import com.zergatul.scripting.type.SReference;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.SVoidType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

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
    private final List<Variable> anonymousLocalSymbols = new ArrayList<>();
    private final boolean isFunctionRoot;
    private final boolean isGenericFunction;
    private final SType returnType;
    private String className;
    private Consumer<MethodVisitor> breakConsumer;
    private Consumer<MethodVisitor> continueConsumer;
    private final List<RefHolder> refVariables = new ArrayList<>();
    private String closureClassName;
    private String asyncStateMachineClassName;
    private final List<LiftedVariable> lifted;
    private final List<CapturedVariable> captured;
    private final FunctionStack stack;

    public CompilerContext() {
        this(1);
    }

    public CompilerContext(int initialStackIndex) {
        this(initialStackIndex, true, SVoidType.instance, null);
    }

    public CompilerContext(int initialStackIndex, boolean isFunctionRoot, SType returnType, CompilerContext parent) {
        this(initialStackIndex, isFunctionRoot, false, returnType, parent);
    }

    public CompilerContext(int initialStackIndex, boolean isFunctionRoot, boolean isGenericFunction, SType returnType, CompilerContext parent) {
        this.root = parent == null ? this : parent.root;
        this.parent = parent;
        this.isFunctionRoot = isFunctionRoot;
        this.isGenericFunction = isGenericFunction;
        this.returnType = returnType;
        if (isFunctionRoot) {
            lifted = new ArrayList<>();
            captured = new ArrayList<>();
        } else {
            lifted = null;
            captured = null;
        }
        stack = new FunctionStack();
        stack.set(initialStackIndex);
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

        LocalVariable variable = new LocalVariable(name, type, definition);
        addLocalVariable(variable);
        return variable;
    }

    public void addLocalVariable(Variable variable) {
        if (variable.getName() != null && hasSymbol(variable.getName())) {
            throw new InternalException();
        }

        insertLocalVariable(variable);
    }

    public LocalVariable addLocalParameter(String name, SType type, TextRange definition) {
        if (name != null && hasSymbol(name)) {
            throw new InternalException();
        }

        LocalVariable variable = new LocalParameter(name, type, definition);
        addLocalVariable(variable);
        return variable;
    }

    public LocalVariable addLocalRefParameter(String name, SReference refType, SType underlying, TextRange definition) {
        if (name == null || name.isEmpty()) {
            throw new InternalException();
        }

        LocalVariable variable = new LocalRefParameter(name, refType, underlying, definition);
        addLocalVariable(variable);
        return variable;
    }

    public void setStackIndex(LocalVariable variable) {
        variable.setStackIndex(stack.get());
        stack.inc(getStackSize(variable.getType()));
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
        return new CompilerContext(stack.get(), false, returnType, this);
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
                    if (!(localSymbol instanceof LiftedVariable)) {
                        LiftedVariable lifted = new LiftedVariable((LocalVariable) localSymbol);
                        for (BoundNameExpressionNode nameExpression : localSymbol.getReferences()) {
                            nameExpression.overrideSymbol(lifted);
                        }
                        localSymbol = lifted;
                        context.getFunctionContext().lifted.add(lifted);
                        context.insertLocalVariable(localSymbol); // replace local variable with lifted
                    }

                    Variable prev = localSymbol;
                    for (int i = functions.size() - 1; i >= 0; i--) {
                        CapturedVariable current = new CapturedVariable(prev);
                        functions.get(i).captured.add(current);
                        functions.get(i).insertLocalVariable(current);
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

    public Variable getVariableOfType(String type) {
        CompilerContext current = this;
        while (true) {
            for (Variable variable : current.anonymousLocalSymbols) {
                if (variable.getType().getInternalName().equals(type)) {
                    return variable;
                }
            }
            for (Variable variable : current.localSymbols.values()) {
                if (variable.getType().getInternalName().equals(type)) {
                    return variable;
                }
            }
            if (current.isFunctionRoot) {
                break;
            }
            current = current.parent;
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

    public String getCurrentClassName() {
        CompilerContext current = this;
        while (true) {
            if (current.className != null) {
                return current.className;
            }
            current = current.parent;
        }
    }

    public List<LiftedVariable> getLifted() {
        return lifted;
    }

    public List<CapturedVariable> getCaptured() {
        return captured;
    }

    public void setClosureClassName(String className) {
        if (isFunctionRoot) {
            closureClassName = className;
        } else {
            throw new InternalException("This is not a function root.");
        }
    }

    public void setAsyncStateMachineClassName(String className) {
        if (isFunctionRoot) {
            asyncStateMachineClassName = className;
        } else {
            throw new InternalException("This is not a function root.");
        }
    }

    public String getAsyncStateMachineClassName() {
        CompilerContext current = this;
        while (true) {
            if (current.parent == null || current.isFunctionRoot) {
                return current.asyncStateMachineClassName;
            }
            current = current.parent;
        }
    }

    private void insertLocalVariable(Variable variable) {
        if (variable.getName() == null) {
            anonymousLocalSymbols.add(variable);
        } else {
            localSymbols.put(variable.getName(), variable);
        }
    }

    private CompilerContext getFunctionContext() {
        CompilerContext current = this;
        while (!current.isFunctionRoot) {
            current = current.parent;
        }
        return current;
    }

    private int getStackSize(SType type) {
        return type == SFloat.instance ? 2 : 1;
    }
}