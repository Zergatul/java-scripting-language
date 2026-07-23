package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.OverloadBinaryOperation;
import com.zergatul.scripting.type.operation.OverloadUnaryOperation;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

public class SDeclaredType extends SReferenceType {

    private final String name;
    private final List<PropertyReference> properties = new ArrayList<>();
    private final List<ConstructorReference> constructors = new ArrayList<>();
    private final List<MethodReference> methods = new ArrayList<>();
    private final List<DeclaredUnaryOperationReference> unaryOperations = new ArrayList<>();
    private final List<DeclaredBinaryOperationReference> binaryOperations = new ArrayList<>();
    private final List<SType> interfaces = new ArrayList<>();
    private @Nullable SType baseType;
    private boolean hasDefaultConstructor;
    private @Nullable String internalName;
    private @Nullable Class<?> clazz;

    public SDeclaredType(String name) {
        this.name = name;
        this.hasDefaultConstructor = true;
        this.constructors.add(new DeclaredConstructorReference(this));
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public void setJavaClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    public DeclaredFieldReference addField(SType type, String name, Visibility visibility) {
        DeclaredFieldReference property = new DeclaredFieldReference(this, type, name, visibility);
        properties.add(property);
        return property;
    }

    public DeclaredConstructorReference addConstructor(SMethodFunction function, Visibility visibility) {
        if (hasDefaultConstructor) {
            constructors.clear();
            hasDefaultConstructor = false;
        }

        DeclaredConstructorReference constructor = new DeclaredConstructorReference(this, function, visibility);
        constructors.add(constructor);
        return constructor;
    }

    public DeclaredMethodReference addMethod(MemberModifiers modifiers, SMethodFunction functionType, String name) {
        DeclaredMethodReference method = new DeclaredMethodReference(this, modifiers, name, functionType);
        methods.add(method);
        return method;
    }

    public boolean hasConcreteImplementation(MethodReference contract) {
        return findImplementation(contract, new HashSet<>(), new HashSet<>()) == Implementation.CONCRETE;
    }

    public DeclaredUnaryOperationReference addUnaryOperation(UnaryOperator operator, SMethodFunction functionType) {
        DeclaredUnaryOperationReference operation = new DeclaredUnaryOperationReference(this, operator, functionType);
        unaryOperations.add(operation);
        return operation;
    }

    public DeclaredBinaryOperationReference addBinaryOperation(BinaryOperator operator, SMethodFunction functionType) {
        DeclaredBinaryOperationReference operation = new DeclaredBinaryOperationReference(this, operator, functionType);
        binaryOperations.add(operation);
        return operation;
    }

    public @NonNull SType getBaseType() {
        return baseType != null ? baseType : SJavaObject.instance;
    }

    public void clearBaseType() {
        this.baseType = null;
    }

    public void setBaseType(SType type) {
        this.baseType = type;
    }

    public void addInterface(SType type) {
        interfaces.add(type);
    }

    public List<SType> getInterfaces() {
        return interfaces;
    }

    // returns how many SDeclaredType's are in inheritance chain
    public int getInheritanceDepth() {
        if (baseType instanceof SDeclaredType base) {
            return 1 + base.getInheritanceDepth();
        } else {
            return 0;
        }
    }

    @Override
    public boolean isInstanceOf(SType other) {
        if (equals(other)) {
            return true;
        }
        if (baseType != null && baseType.isInstanceOf(other)) {
            return true;
        }
        if (interfaces.stream().anyMatch(i -> i.isInstanceOf(other))) {
            return true;
        }
        return !other.isSyntheticType() && other.equals(SJavaObject.instance);
    }

    @Override
    public String getInternalName() {
        if (internalName == null) {
            throw new InternalException();
        }
        return internalName;
    }

    @Override
    public Class<?> getJavaClass() {
        if (clazz == null) {
            throw new InternalException();
        } else {
            return clazz;
        }
    }

    @Override
    public boolean isAssignableFrom(SType other) {
        if (other == this) {
            return true;
        }
        if (other instanceof SDeclaredType declaredType && declaredType.baseType != null) {
            return isAssignableFrom(declaredType.baseType);
        }
        if (other instanceof SDeclaredType declaredType) {
            return declaredType.interfaces.stream().anyMatch(this::isAssignableFrom);
        }
        return false;
    }

    @Override
    public String getDescriptor() {
        if (internalName == null) {
            throw new InternalException();
        } else {
            return Type.getObjectType(internalName).getDescriptor();
        }
    }

    @Override
    public boolean hasDefaultValue() {
        return false;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        throw new InternalException();
    }

    @Override
    public List<ConstructorReference> getConstructors() {
        return constructors;
    }

    @Override
    public List<UnaryOperation> getUnaryOperations() {
        return new ArrayList<>(unaryOperations.stream().map(OverloadUnaryOperation::new).toList());
    }

    @Override
    public List<BinaryOperation> getBinaryOperations() {
        List<BinaryOperation> operations = new ArrayList<>();
        operations.addAll(binaryOperations.stream().map(OverloadBinaryOperation::new).toList());
        operations.addAll(super.getBinaryOperations());
        return operations;
    }

    @Override
    public List<PropertyReference> getDeclaredProperties() {
        return properties;
    }

    @Override
    public List<MethodReference> getDeclaredMethods() {
        return methods;
    }

    private Implementation findImplementation(
            MethodReference contract,
            Set<SDeclaredType> visitedDeclaredTypes,
            Set<Class<?>> visitedJavaTypes
    ) {
        if (!visitedDeclaredTypes.add(this)) {
            return Implementation.NONE;
        }

        Implementation implementation = findDeclaredImplementation(methods, contract);
        if (implementation != Implementation.NONE) {
            return implementation;
        }

        if (baseType != null) {
            implementation = findImplementation(baseType, contract, visitedDeclaredTypes, visitedJavaTypes);
            if (implementation != Implementation.NONE) {
                return implementation;
            }
        }

        implementation = Implementation.NONE;
        for (SType interfaceType : interfaces) {
            implementation = Implementation.combine(
                    implementation,
                    findImplementation(interfaceType, contract, visitedDeclaredTypes, visitedJavaTypes));
        }
        return implementation;
    }

    private static Implementation findImplementation(
            SType type,
            MethodReference contract,
            Set<SDeclaredType> visitedDeclaredTypes,
            Set<Class<?>> visitedJavaTypes
    ) {
        if (type instanceof SDeclaredType declaredType) {
            return declaredType.findImplementation(contract, visitedDeclaredTypes, visitedJavaTypes);
        }
        if (type instanceof SClassType || type instanceof SCustomType) {
            return findJavaImplementation(type.getJavaClass(), contract, visitedJavaTypes);
        }
        return Implementation.NONE;
    }

    private static Implementation findJavaImplementation(
            Class<?> clazz,
            MethodReference contract,
            Set<Class<?>> visitedJavaTypes
    ) {
        if (!visitedJavaTypes.add(clazz)) {
            return Implementation.NONE;
        }

        Implementation implementation = findDeclaredJavaImplementation(clazz, contract);
        if (implementation != Implementation.NONE) {
            return implementation;
        }

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            implementation = findJavaImplementation(superclass, contract, visitedJavaTypes);
            if (implementation != Implementation.NONE) {
                return implementation;
            }
        }

        implementation = Implementation.NONE;
        for (Class<?> interfaceClass : clazz.getInterfaces()) {
            implementation = Implementation.combine(
                    implementation,
                    findJavaImplementation(interfaceClass, contract, visitedJavaTypes));
        }
        return implementation;
    }

    private static Implementation findDeclaredJavaImplementation(Class<?> clazz, MethodReference contract) {
        Implementation implementation = Implementation.NONE;
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || !signatureMatches(method, contract)) {
                continue;
            }

            NativeMethodReference reference = new NativeMethodReference(method);
            if (reducesVisibility(reference.getVisibility(), contract.getVisibility())) {
                continue;
            }

            implementation = Implementation.combine(
                    implementation,
                    Modifier.isAbstract(method.getModifiers()) ?
                            Implementation.ABSTRACT :
                            Implementation.CONCRETE);
        }
        return implementation;
    }

    private static Implementation findDeclaredImplementation(
            List<MethodReference> methods,
            MethodReference contract
    ) {
        Implementation implementation = Implementation.NONE;
        for (MethodReference method : methods) {
            if (method.isStatic() || !signatureMatches(method, contract)) {
                continue;
            }
            if (reducesVisibility(method.getVisibility(), contract.getVisibility())) {
                continue;
            }

            implementation = Implementation.combine(
                    implementation,
                    method.isAbstract() ? Implementation.ABSTRACT : Implementation.CONCRETE);
        }
        return implementation;
    }

    private static boolean signatureMatches(Method method, MethodReference contract) {
        if (!method.getName().equals(contract.getName())) {
            return false;
        }

        if (contract instanceof NativeMethodReference nativeContract) {
            Method contractMethod = nativeContract.getUnderlying();
            return method.getReturnType() == contractMethod.getReturnType() &&
                    Arrays.equals(method.getParameterTypes(), contractMethod.getParameterTypes());
        }

        return SType.fromJavaType(method.getReturnType()).equals(contract.getReturn()) &&
                Arrays.stream(method.getParameterTypes())
                        .map(SType::fromJavaType)
                        .toList()
                        .equals(contract.getParameterTypes());
    }

    private static boolean signatureMatches(MethodReference method, MethodReference contract) {
        return method.getName().equals(contract.getName()) &&
                method.getReturn().equals(contract.getReturn()) &&
                method.getParameterTypes().equals(contract.getParameterTypes());
    }

    private static boolean reducesVisibility(Visibility visibility, Visibility contractVisibility) {
        return switch (contractVisibility) {
            case PUBLIC -> visibility != Visibility.PUBLIC;
            case PROTECTED -> visibility == Visibility.PRIVATE;
            case PRIVATE -> false;
        };
    }

    @Override
    public String toString() {
        return name;
    }

    private enum Implementation {
        NONE,
        ABSTRACT,
        CONCRETE;

        private static Implementation combine(Implementation first, Implementation second) {
            if (first == CONCRETE || second == CONCRETE) {
                return CONCRETE;
            }
            if (first == ABSTRACT || second == ABSTRACT) {
                return ABSTRACT;
            }
            return NONE;
        }
    }
}