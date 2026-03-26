package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.frames.Frame;
import com.zergatul.scripting.compiler.frames.FunctionFrame;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;
import java.util.*;

public class CompilerContext {

    private final CompilerContext root;
    private final @Nullable CompilerContext parent;
    private final Map<String, SymbolRef> staticSymbols = new HashMap<>();
    private final Map<String, SymbolRef> localSymbols = new HashMap<>();
    private final List<SymbolRef> anonymousLocalSymbols = new ArrayList<>();
    private final boolean isClassRoot;
    private final SDeclaredType classType;
    private final @Nullable SType extensionType;
    private final boolean isStatic;
    private final boolean isClassMethod;
    private final boolean isFunctionRoot;
    private final boolean isGenericFunction;
    private final SType returnType;
    private final boolean isAsync;
    private final @Nullable Frame frame;
    private String className;
    private final List<RefHolder> refVariables = new ArrayList<>();
    private String thisFieldName;
    private final List<LiftedVariable> lifted;
    private final List<CapturedVariable> captured;
    private final FunctionStack stack;
    private @Nullable JavaInteropPolicy interopPolicy;
    private int lastEmittedLine;
    private final List<SGenericFunction> genericFunctions;
    private Label startLabel;
    private ClassLoaderContext classLoaderContext;
    private @Nullable MethodHandleCache methodHandleCache;
    private @Nullable AsyncStateMachineContext asyncContext;

    private CompilerContext(
            @Nullable CompilerContext parent,
            int initialStackIndex,
            boolean isFunctionRoot,
            boolean isGenericFunction,
            SType returnType,
            boolean isAsync,
            boolean isClassRoot,
            SDeclaredType classType,
            @Nullable SType extensionType,
            boolean isClassMethod,
            boolean isStatic,
            @Nullable Frame frame
    ) {
        this.root = parent == null ? this : parent.root;
        this.parent = parent;
        this.isClassRoot = isClassRoot;
        this.classType = classType;
        this.extensionType = extensionType;
        this.isFunctionRoot = isFunctionRoot;
        this.isGenericFunction = isGenericFunction;
        this.returnType = returnType;
        this.isAsync = isAsync;
        this.isStatic = isStatic;
        this.isClassMethod = isClassMethod;
        this.frame = frame;
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
        return createChild(getFrame());
    }

    public CompilerContext createChild(Frame frame) {
        return new Builder()
                .setParent(this)
                .setClassType(this.classType)
                .setExtensionType(this.extensionType)
                .setClassMethod(this.isClassMethod)
                .setInitialStackIndex(stack.get())
                .setGenericFunction(isGenericFunction)
                .setReturnType(returnType)
                .setAsync(isAsync)
                .setFrame(frame)
                .build();
    }

    public CompilerContext createStaticFunction(SType returnType, boolean isAsync) {
        return new CompilerContext.Builder()
                .setParent(this)
                .setFunctionRoot(true)
                .setReturnType(returnType)
                .setAsync(isAsync)
                .setFrame(new FunctionFrame())
                .build();
    }

    public CompilerContext createInstanceMethod(SType returnType, boolean isAsync) {
        return createInstanceMethod(returnType, isAsync, false);
    }

    public CompilerContext createInstanceMethod(SType returnType, boolean isAsync, boolean generic) {
        return new Builder()
                .setParent(this)
                .setInitialStackIndex(1)
                .setFunctionRoot(true)
                .setGenericFunction(generic)
                .setReturnType(returnType)
                .setAsync(isAsync)
                .setFrame(new FunctionFrame())
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
                .setExtensionType(extensionType)
                .setClassMethod(true)
                .setFunctionRoot(true)
                .setReturnType(returnType)
                .setInitialStackIndex(1)
                .setAsync(isAsync)
                .setFrame(new FunctionFrame())
                .build();
    }

    public CompilerContext createClassStaticMethod(SType returnType) {
        return new Builder()
                .setParent(this)
                .setClassType(classType)
                .setExtensionType(extensionType)
                .setStatic()
                .setClassMethod(true)
                .setFunctionRoot(true)
                .setReturnType(returnType)
                .setInitialStackIndex(0)
                .setAsync(false)
                .setFrame(new FunctionFrame())
                .build();
    }

    public CompilerContext createExtension(SType type) {
        return new Builder()
                .setParent(this)
                .setClassRoot(true)
                .setExtensionType(type)
                .build();
    }

    public CompilerContext createExtensionMethod(SType returnType, boolean isAsync) {
        return new Builder()
                .setParent(this)
                .setExtensionType(extensionType)
                .setFunctionRoot(true)
                .setReturnType(returnType)
                .setAsync(isAsync)
                .setFrame(new FunctionFrame())
                .build();
    }

    public SType getReturnType() {
        return returnType;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public CompilerContext getParent() {
        if (parent == null) {
            throw new InternalException();
        }
        return parent;
    }

    public boolean isClassMethod() {
        return isClassMethod;
    }

    public boolean isDeclaredClass() {
        return getClassRootContext().classType != null;
    }

    public boolean isExtension() {
        return getClassRootContext().extensionType != null;
    }

    public SDeclaredType getClassType() {
        if (classType == null) {
            throw new InternalException();
        }

        return classType;
    }

    public SType getExtensionType() {
        if (extensionType == null) {
            throw new InternalException();
        }

        return extensionType;
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

    public @Nullable SymbolRef getSymbol(String name) {
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

                    return functions.getFirst().localSymbols.get(name);
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
            if (func.signatureMatchesWithBoxing(returnType, parameters)) {
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

    public Frame getFrame() {
        if (this.frame == null) {
            throw new InternalException();
        }
        return this.frame;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return root.className;
    }

    public List<LiftedVariable> getLifted() {
        return lifted;
    }

    public List<CapturedVariable> getCaptured() {
        return captured;
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
        return root.interopPolicy == null || root.interopPolicy.isMethodVisible(method);
    }

    public boolean isJavaTypeUsageAllowed() {
        return root.interopPolicy == null || root.interopPolicy.isJavaTypeUsageAllowed();
    }

    public ClassLoader getJavaTypeClassLoader() {
        return root.interopPolicy == null ? Thread.currentThread().getContextClassLoader() : root.interopPolicy.getClassLoader();
    }

    public String getJavaTypeUsageError() {
        return root.interopPolicy.getJavaTypeUsageError();
    }

    public void setInteropPolicy(@Nullable JavaInteropPolicy interopPolicy) {
        this.interopPolicy = interopPolicy;
    }

    public int getLastEmittedLine() {
        return getFunctionContext().lastEmittedLine;
    }

    public void setLastEmittedLine(int line) {
        getFunctionContext().lastEmittedLine = line;
    }

    public void setClassLoaderContext(ClassLoaderContext classLoaderContext) {
        this.classLoaderContext = classLoaderContext;
    }

    public Class<?> defineClass(String name, byte[] code) {
        if (root.classLoaderContext == null) {
            throw new InternalException();
        }

        return root.classLoaderContext.defineClass(name, code);
    }

    public int getNextUniqueIndex() {
        if (root.classLoaderContext == null) {
            throw new InternalException();
        }

        return root.classLoaderContext.getNextUniqueIndex();
    }

    public String createCachedPrivateFieldHandle(PropertyReference property) {
        if (property instanceof FieldPropertyReference fieldProperty) {
            if (root.methodHandleCache == null) {
                root.methodHandleCache = new MethodHandleCache();
            }
            return root.methodHandleCache.createFieldAccess(fieldProperty.getUnderlyingField());
        } else {
            throw new InternalException();
        }
    }

    public String createCachedPrivateMethodHandle(MethodReference method) {
        if (method instanceof NativeMethodReference methodReference) {
            if (root.methodHandleCache == null) {
                root.methodHandleCache = new MethodHandleCache();
            }
            return root.methodHandleCache.createMethodAccess(methodReference.getUnderlying());
        } else {
            throw new InternalException();
        }
    }

    public @Nullable MethodHandleCache getMethodHandleCache() {
        return methodHandleCache;
    }

    public AsyncStateMachineContext getAsyncContext() {
        AsyncStateMachineContext context = getFunctionContext().asyncContext;
        if (context == null) {
            throw new InternalException();
        }
        return context;
    }

    public void setAsyncContext(AsyncStateMachineContext asyncContext) {
        this.asyncContext = asyncContext;
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

        private @Nullable CompilerContext parent;
        private @Nullable Frame frame;
        private boolean isClassRoot;
        private SDeclaredType classType;
        private SType extensionType;
        private boolean isStatic;
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

        public Builder setExtensionType(SType value) {
            this.extensionType = value;
            return this;
        }

        public Builder setStatic() {
            this.isStatic = true;
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

        public Builder setFrame(Frame frame) {
            this.frame = frame;
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
                    extensionType,
                    isClassMethod,
                    isStatic,
                    frame);
        }
    }
}