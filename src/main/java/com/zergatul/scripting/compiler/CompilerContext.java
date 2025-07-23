package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.SReference;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.*;
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
    private final boolean isAsync;
    private String className;
    private Consumer<MethodVisitor> breakConsumer;
    private Consumer<MethodVisitor> continueConsumer;
    private final List<RefHolder> refVariables = new ArrayList<>();
    private String asyncStateMachineClassName;
    private final List<LiftedVariable> lifted;
    private final List<CapturedVariable> captured;
    private final FunctionStack stack;
    private Label generatorContinueLabel;
    private JavaInteropPolicy policy;
    private int lastEmittedLine;

    public CompilerContext(SType returnType, boolean isAsync) {
        this(1, true, returnType, isAsync, null);
    }

    public CompilerContext(int initialStackIndex, boolean isFunctionRoot, SType returnType, boolean isAsync, CompilerContext parent) {
        this(initialStackIndex, isFunctionRoot, false, returnType, isAsync, parent);
    }

    public CompilerContext(int initialStackIndex, boolean isFunctionRoot, boolean isGenericFunction, SType returnType, boolean isAsync, CompilerContext parent) {
        this.root = parent == null ? this : parent.root;
        this.parent = parent;
        this.isFunctionRoot = isFunctionRoot;
        this.isGenericFunction = isGenericFunction;
        this.returnType = returnType;
        this.isAsync = isAsync;
        if (isFunctionRoot) {
            lifted = new ArrayList<>();
            captured = new ArrayList<>();
        } else {
            lifted = null;
            captured = null;
        }
        stack = new FunctionStack();
        stack.set(initialStackIndex);
        lastEmittedLine = -1;
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
        if (name != null) {
            Symbol symbol = getSymbol(name);
            if (symbol instanceof LocalVariable) {
                throw new InternalException();
            }
        }

        LocalVariable variable = new LocalVariable(name, type, definition);
        addLocalVariable(variable);
        return variable;
    }

    public void addLocalVariable(Variable variable) {
        if (variable.getName() != null) {
            Symbol symbol = getSymbol(variable.getName());
            if (symbol instanceof LocalVariable) {
                throw new InternalException();
            }
        }

        insertLocalVariable(variable);
    }

    public ExternalParameter addExternalParameter(String name, SType type, int index) {
        if (name != null && hasSymbol(name)) {
            throw new InternalException();
        }

        ExternalParameter parameter = new ExternalParameter(name, type, index);
        addLocalVariable(parameter);
        return parameter;
    }

    public LocalVariable addLocalParameter(String name, SType type, TextRange definition) {
        if (name != null) {
            Symbol symbol = getSymbol(name);
            if (symbol instanceof LocalVariable) {
                throw new InternalException();
            }
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
        List<RefHolder> variables = List.of(refVariables.toArray(RefHolder[]::new));
        refVariables.clear();
        return variables;
    }

    public CompilerContext createChild() {
        return new CompilerContext(stack.get(), false, isGenericFunction, returnType, isAsync, this);
    }

    public CompilerContext createStaticFunction(SType returnType, boolean isAsync) {
        return new CompilerContext(0, true, returnType, isAsync, this);
    }

    public CompilerContext createFunction(SType returnType, boolean isAsync) {
        return createFunction(returnType, isAsync, false);
    }

    public CompilerContext createFunction(SType returnType, boolean isAsync, boolean generic) {
        return new CompilerContext(1, true, generic, returnType, isAsync, this);
    }

    public SType getReturnType() {
        return returnType;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public CompilerContext getParent() {
        return parent;
    }

    public Collection<Symbol> getStaticSymbols() {
        return staticSymbols.values();
    }

    public Symbol getSymbol(String name) {
        List<CompilerContext> functions = List.of(); // function boundaries
        for (CompilerContext context = this; context != null; ) {
            Variable localSymbol = context.localSymbols.get(name);
            if (localSymbol != null) {
                if (functions.isEmpty()) {
                    return localSymbol;
                } else {
                    if (!(localSymbol instanceof LiftedVariable) && !(localSymbol instanceof CapturedVariable)) {
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

        Symbol staticSymbol = root.staticSymbols.get(name);
        if (staticSymbol != null) {
            return staticSymbol;
        }

        return null;
    }

    public List<ExternalParameter> getExternalParameters() {
        return localSymbols.values().stream()
                .filter(v -> v instanceof ExternalParameter)
                .map(v -> (ExternalParameter) v)
                .toList();
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

    public Label getGeneratorContinueLabel() {
        return getFunctionContext().generatorContinueLabel;
    }

    public void setGeneratorContinueLabel(Label label) {
        generatorContinueLabel = label;
    }

    public void reserveStack(int index) {
        stack.set(index);
    }

    public void markEnd(MethodVisitor visitor) {
        Label label = new Label();
        visitor.visitLabel(label);
        for (Variable variable : localSymbols.values()) {
            if (variable instanceof LocalVariable local) {
                visitor.visitLocalVariable(
                        local.getName(),
                        Type.getDescriptor(local.getType().getJavaClass()),
                        null,
                        local.getDeclarationLabel(),
                        label,
                        local.getStackIndex());
            }
        }
    }

    public boolean isMethodVisible(Method method) {
        return root.policy == null || root.policy.isMethodVisible(method);
    }

    public boolean isJavaTypeUsageAllowed() {
        return root.policy == null || root.policy.isJavaTypeUsageAllowed();
    }

    public String getJavaTypeUsageError() {
        return root.policy.getJavaTypeUsageError();
    }

    public void setPolicy(JavaInteropPolicy policy) {
        this.policy = policy;
    }

    public int getLastEmittedLine() {
        return getFunctionContext().lastEmittedLine;
    }

    public void setLastEmittedLine(int line) {
        getFunctionContext().lastEmittedLine = line;
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
        return type.isJvmCategoryOneComputationalType() ? 1 : 2;
    }
}