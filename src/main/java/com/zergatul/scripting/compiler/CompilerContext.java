package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public class CompilerContext {

    private final CompilerContext root;
    private final CompilerContext parent;
    private final Map<String, SymbolRef> staticSymbols = new HashMap<>();
    private final Map<String, SymbolRef> localSymbols = new HashMap<>();
    private final List<SymbolRef> anonymousLocalSymbols = new ArrayList<>();
    private final boolean isClassRoot;
    private final SDeclaredType classType;
    private final boolean isClassMethod;
    private final boolean isFunctionRoot;
    private final boolean isGenericFunction;
    private final SType returnType;
    private final boolean isAsync;
    private String className;
    private Consumer<MethodVisitor> breakConsumer;
    private Consumer<MethodVisitor> continueConsumer;
    private final List<RefHolder> refVariables = new ArrayList<>();
    private String asyncStateMachineClassName;
    private String thisFieldName;
    private final List<LiftedVariable> lifted;
    private final List<CapturedVariable> captured;
    private final FunctionStack stack;
    private Label generatorContinueLabel;
    private JavaInteropPolicy policy;
    private int lastEmittedLine;
    private final List<SGenericFunction> genericFunctions;
    private Label startLabel;

    private CompilerContext(
            CompilerContext parent,
            int initialStackIndex,
            boolean isFunctionRoot,
            boolean isGenericFunction,
            SType returnType,
            boolean isAsync,
            boolean isClassRoot,
            SDeclaredType classType,
            boolean isClassMethod
    ) {
        this.root = parent == null ? this : parent.root;
        this.parent = parent;
        this.isClassRoot = isClassRoot;
        this.classType = classType;
        this.isFunctionRoot = isFunctionRoot;
        this.isGenericFunction = isGenericFunction;
        this.returnType = returnType;
        this.isAsync = isAsync;
        this.isClassMethod = isClassMethod;
        if (isFunctionRoot) {
            lifted = new ArrayList<>();
            captured = new ArrayList<>();
        } else {
            lifted = null;
            captured = null;
        }
        if (isClassRoot) {
            stack = null;
        } else {
            stack = new FunctionStack();
            stack.set(initialStackIndex);
        }
        lastEmittedLine = -1;

        if (parent == null) {
            genericFunctions = new ArrayList<>();
        } else {
            genericFunctions = null;
        }
    }

    public static CompilerContext create(SType returnType, boolean isAsync) {
        return new Builder()
                .setFunctionRoot(true)
                .setInitialStackIndex(1)
                .setReturnType(returnType)
                .setAsync(isAsync)
                .build();
    }

    public void addStaticSymbol(String name, SymbolRef symbolRef) {
        if (hasSymbol(name)) {
            throw new InternalException();
        }

        staticSymbols.put(name, symbolRef);
    }

    public SymbolRef addLocalVariable(String name, SType type, TextRange definition) {
        LocalVariable variable = new LocalVariable(name, type, definition);
        SymbolRef symbolRef = new MutableSymbolRef(variable);
        addLocalVariable(symbolRef);
        return symbolRef;
    }

    public void addLocalVariable(SymbolRef variableRef) {
        if (variableRef.get().getName() != null) {
            SymbolRef symbolRef = getSymbol(variableRef.get().getName());
            if (symbolRef != null && symbolRef.get() instanceof LocalVariable) {
                throw new InternalException();
            }
        }

        insertLocalVariable(variableRef);
    }

    public ExternalParameter addExternalParameter(String name, SType type, int index) {
        if (name != null && hasSymbol(name)) {
            throw new InternalException();
        }

        ExternalParameter parameter = new ExternalParameter(name, type, index);
        addLocalVariable(new MutableSymbolRef(parameter));
        return parameter;
    }

    public LocalVariable addLocalParameter(String name, SType type, TextRange definition) {
        if (name != null) {
            SymbolRef symbolRef = getSymbol(name);
            if (symbolRef != null && symbolRef.get() instanceof LocalVariable) {
                throw new InternalException();
            }
        }

        LocalVariable variable = new LocalParameter(name, type, definition);
        addLocalVariable(new MutableSymbolRef(variable));
        return variable;
    }

    public SymbolRef addLocalParameter2(String name, SType type, TextRange definition) {
        LocalVariable variable = new LocalParameter(name, type, definition);
        SymbolRef symbolRef = new MutableSymbolRef(variable);
        addLocalVariable(symbolRef);
        return symbolRef;
    }

    public LocalVariable addLocalRefParameter(String name, SByReference refType, SType underlying, TextRange definition) {
        if (name == null || name.isEmpty()) {
            throw new InternalException();
        }

        LocalVariable variable = new LocalRefParameter(name, refType, underlying, definition);
        addLocalVariable(new MutableSymbolRef(variable));
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
        SymbolRef symbolRef = addLocalVariable(null, variable.getType().getReferenceType(), null);
        LocalVariable holder = symbolRef.asLocalVariable();
        refVariables.add(new RefHolder(holder, variable));
        return holder;
    }

    public List<RefHolder> releaseRefVariables() {
        List<RefHolder> variables = List.of(refVariables.toArray(RefHolder[]::new));
        refVariables.clear();
        return variables;
    }

    public void setStartLabel(Label label) {
        startLabel = label;
    }

    public CompilerContext createChild() {
        return new Builder()
                .setParent(this)
                .setClassType(this.classType)
                .setClassMethod(this.isClassMethod)
                .setInitialStackIndex(stack.get())
                .setGenericFunction(isGenericFunction)
                .setReturnType(returnType)
                .setAsync(isAsync)
                .build();
    }

    public CompilerContext createStaticFunction(SType returnType, boolean isAsync) {
        return new CompilerContext.Builder()
                .setParent(this)
                .setFunctionRoot(true)
                .setReturnType(returnType)
                .setAsync(isAsync)
                .build();
    }

    public CompilerContext createFunction(SType returnType, boolean isAsync) {
        return createFunction(returnType, isAsync, false);
    }

    public CompilerContext createFunction(SType returnType, boolean isAsync, boolean generic) {
        return new Builder()
                .setParent(this)
                .setInitialStackIndex(1)
                .setFunctionRoot(true)
                .setGenericFunction(generic)
                .setReturnType(returnType)
                .setAsync(isAsync)
                .build();
    }

    public CompilerContext createClass(SDeclaredType type) {
        return new Builder()
                .setParent(this)
                .setClassRoot(true)
                .setClassType(type)
                .build();
    }

    public CompilerContext createClassMethod(SType returnType, boolean isAsync) {
        return new Builder()
                .setParent(this)
                .setClassType(classType)
                .setClassMethod(true)
                .setFunctionRoot(true)
                .setReturnType(returnType)
                .setInitialStackIndex(1)
                .setAsync(isAsync)
                .build();
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

    public boolean isClassMethod() {
        return isClassMethod;
    }

    public SType getClassType() {
        if (classType == null) {
            throw new InternalException();
        }

        return classType;
    }

    public Collection<SymbolRef> getStaticSymbols() {
        return staticSymbols.values();
    }

    public boolean hasLocalSymbol(String name) {
        CompilerContext current = this;
        while (true) {
            if (current.localSymbols.containsKey(name)) {
                return true;
            }
            if (current.isFunctionRoot) {
                return false;
            }
            current = current.parent;
        }
    }

    public SymbolRef getSymbol(String name) {
        List<CompilerContext> functions = List.of(); // function boundaries
        for (CompilerContext context = this; context != null; ) {
            SymbolRef localSymbolRef = context.localSymbols.get(name);
            if (localSymbolRef != null) {
                if (functions.isEmpty()) {
                    return localSymbolRef;
                } else {
                    Variable original = localSymbolRef.asVariable();
                    if (!(localSymbolRef.get() instanceof LiftedVariable) && !(localSymbolRef.get() instanceof CapturedVariable)) {
                        LiftedVariable lifted = new LiftedVariable(localSymbolRef.asLocalVariable());
                        localSymbolRef.set(lifted); // updates all references
                        original = lifted;
                        context.getFunctionContext().lifted.add(lifted);
                    }

                    Variable prev = original;
                    for (int i = functions.size() - 1; i >= 0; i--) {
                        CapturedVariable current = new CapturedVariable(prev);
                        MutableSymbolRef currentSymbolRef = new MutableSymbolRef(current);
                        functions.get(i).captured.add(current);
                        functions.get(i).insertLocalVariable(currentSymbolRef);
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

        SymbolRef staticSymbolRef = root.staticSymbols.get(name);
        if (staticSymbolRef != null) {
            return staticSymbolRef;
        }

        return null;
    }

    public Variable getVariableOfType(String type) {
        CompilerContext current = this;
        while (true) {
            for (SymbolRef ref : current.anonymousLocalSymbols) {
                if (ref.get().getType().getInternalName().equals(type)) {
                    return ref.asVariable();
                }
            }
            for (SymbolRef ref : current.localSymbols.values()) {
                if (ref.get().getType().getInternalName().equals(type)) {
                    return ref.asVariable();
                }
            }
            if (current.isFunctionRoot) {
                break;
            }
            current = current.parent;
        }
        return null;
    }

    public List<SGenericFunction> getGenericFunctions() {
        return genericFunctions;
    }

    public SGenericFunction getGenericFunction(SType returnType, SType[] parameters) {
        for (SGenericFunction func : root.genericFunctions) {
            if (func.matches(returnType, parameters)) {
                return func;
            }
        }

        SGenericFunction genericFunction = new SGenericFunction(returnType, parameters);
        root.genericFunctions.add(genericFunction);
        return genericFunction;
    }

    public void copyGenericFunctionsFrom(CompilerContext other) {
        genericFunctions.addAll(other.getGenericFunctions());
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

    public void setAsyncThisFieldName(String thisFieldName) {
        if (isFunctionRoot) {
            this.thisFieldName = thisFieldName;
        } else {
            throw new InternalException("This is not a function root.");
        }
    }

    public String getAsyncThisFieldName() {
        CompilerContext current = this;
        while (true) {
            if (current.parent == null || current.isFunctionRoot) {
                return current.thisFieldName;
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

    public void emitLocalVariableTable(MethodVisitor visitor, List<LocalVariable> variables) {
        Label endLabel = new Label();
        visitor.visitLabel(endLabel);
        for (SymbolRef ref : localSymbols.values()) {
            if (ref.get() instanceof LocalVariable local) {
                if (local.getName() == null || local.getName().isEmpty()) {
                    continue;
                }
                visitor.visitLocalVariable(
                        local.getName(),
                        local.getType().getDescriptor(),
                        null,
                        startLabel,
                        endLabel,
                        local.getStackIndex());
            }
        }
        for (LocalVariable local : variables) {
            if (local.getName() == null || local.getName().isEmpty()) {
                continue;
            }
            visitor.visitLocalVariable(
                    local.getName(),
                    local.getType().getDescriptor(),
                    null,
                    startLabel,
                    endLabel,
                    local.getStackIndex());
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

    private void insertLocalVariable(SymbolRef variableRef) {
        if (variableRef.get().getName() == null) {
            anonymousLocalSymbols.add(variableRef);
        } else {
            localSymbols.put(variableRef.get().getName(), variableRef);
        }
    }

    private CompilerContext getClassRootContext() {
        CompilerContext current = this;
        while (!current.isClassRoot) {
            current = current.parent;
        }
        return current;
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

    private static class Builder {

        private CompilerContext parent;
        private boolean isClassRoot;
        private SDeclaredType classType;
        private boolean isClassMethod;
        public SType returnType;
        public boolean isAsync;
        public int initialStackIndex;
        public boolean isFunctionRoot;
        public boolean isGenericFunction;

        public Builder setParent(CompilerContext value) {
            this.parent = value;
            return this;
        }

        public Builder setClassRoot(boolean value) {
            this.isClassRoot = value;
            return this;
        }

        public Builder setClassType(SDeclaredType value) {
            this.classType = value;
            return this;
        }

        public Builder setClassMethod(boolean value) {
            this.isClassMethod = value;
            return this;
        }

        public Builder setReturnType(SType value) {
            this.returnType = value;
            return this;
        }

        public Builder setAsync(boolean value) {
            this.isAsync = value;
            return this;
        }

        public Builder setInitialStackIndex(int value) {
            this.initialStackIndex = value;
            return this;
        }

        public Builder setFunctionRoot(boolean value) {
            this.isFunctionRoot = value;
            return this;
        }

        public Builder setGenericFunction(boolean value) {
            this.isGenericFunction = value;
            return this;
        }

        public CompilerContext build() {
            return new CompilerContext(
                    parent,
                    initialStackIndex,
                    isFunctionRoot,
                    isGenericFunction,
                    returnType,
                    isAsync,
                    isClassRoot,
                    classType,
                    isClassMethod);
        }
    }
}