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

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SDeclaredType extends SReferenceType {

    private final String name;
    private final List<PropertyReference> properties = new ArrayList<>();
    private final List<ConstructorReference> constructors = new ArrayList<>();
    private final List<MethodReference> methods = new ArrayList<>();
    private final List<DeclaredUnaryOperationReference> unaryOperations = new ArrayList<>();
    private final List<DeclaredBinaryOperationReference> binaryOperations = new ArrayList<>();
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

    public DeclaredFieldReference addField(SType type, String name) {
        DeclaredFieldReference property = new DeclaredFieldReference(this, type, name);
        properties.add(property);
        return property;
    }

    public DeclaredConstructorReference addConstructor(SMethodFunction function) {
        if (hasDefaultConstructor) {
            constructors.clear();
            hasDefaultConstructor = false;
        }

        DeclaredConstructorReference constructor = new DeclaredConstructorReference(this, function);
        constructors.add(constructor);
        return constructor;
    }

    public DeclaredMethodReference addMethod(MemberModifiers modifiers, SMethodFunction functionType, String name) {
        DeclaredMethodReference method = new DeclaredMethodReference(this, modifiers, name, functionType);
        methods.add(method);
        return method;
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
    public List<PropertyReference> getInstanceProperties() {
        if (baseType != null) {
            List<PropertyReference> combined = new ArrayList<>();
            combined.addAll(baseType.getInstanceProperties());
            combined.addAll(properties);
            return combined;
        } else {
            return properties;
        }
    }

    public List<MethodReference> getInstanceMethods() {
        if (getBaseType() == SJavaObject.instance) {
            return getDeclaredInstanceMethods();
        }

        List<MethodReference> methods = new ArrayList<>();

        SType current = this;
        while (current != null && current != SJavaObject.instance) {
            methods.addAll(current.getDeclaredInstanceMethods());
            current = current.getBaseType();
        }

        return methods;
    }

    @Override
    public List<MethodReference> getDeclaredInstanceMethods() {
        return methods;
    }

    @Override
    public String toString() {
        return name;
    }
}