package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.SDeclaredType;
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
    private final Map<String, Symbol> classSymbols;
    private final List<Constructor> classConstructors;
    private final List<Variable> anonymousLocalSymbols = new ArrayList<>();
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
    private final List<LiftedVariable> lifted;
    private final List<CapturedVariable> captured;
    private final FunctionStack stack;
    private Label generatorContinueLabel;
    private JavaInteropPolicy policy;
    private int lastEmittedLine;

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
            classSymbols = new HashMap<>();
            classConstructors = new ArrayList<>();
            stack = null;
        } else {
            classSymbols = null;
            classConstructors = null;
            stack = new FunctionStack();
            stack.set(initialStackIndex);
        }
        lastEmittedLine = -1;
    }

    public static CompilerContext create(SType returnType, boolean isAsync) {
        return new Builder()
                .setFunctionRoot(true)
                .setInitialStackIndex(1)
                .setReturnType(returnType)
                .setAsync(isAsync)
                .build();
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

    public void addClass(ClassSymbol classSymbol) {
        if (hasSymbol(classSymbol.getName())) {
            throw new InternalException();
        }

        staticSymbols.put(classSymbol.getName(), classSymbol);
    }

    public void addClassMember(Symbol symbol) {
        if (classType == null) {
            throw new InternalException();
        }

        classSymbols.put(symbol.getName(), symbol);
    }

    public void addClassConstructor(Constructor constructor) {
        if (classType == null) {
            throw new InternalException();
        }

        classConstructors.add(constructor);
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

    public CompilerContext createClassMethod(SType returnType) {
        return new Builder()
                .setParent(this)
                .setClassType(classType)
                .setClassMethod(true)
                .setFunctionRoot(true)
                .setReturnType(returnType)
                .setInitialStackIndex(1)
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

    public Symbol getClassSymbol(String name) {
        // TODO: add field reference to class root???
        return getClassRootContext().classSymbols.get(name);
    }

    public Collection<Constructor> getConstructors() {
        return classConstructors;
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