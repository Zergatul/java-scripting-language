package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SDeclaredType extends SType {

    private final String name;
    private final List<PropertyReference> properties = new ArrayList<>();
    private final List<ConstructorReference> constructors = new ArrayList<>();
    private final List<MethodReference> methods = new ArrayList<>();
    @Nullable private SType baseType;
    private boolean hasDefaultConstructor;
    @Nullable private String internalName;
    @Nullable private Class<?> clazz;

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

    public void addField(SType type, String name) {
        properties.add(new DeclaredFieldReference(this, type, name));
    }

    public void addConstructor(SMethodFunction function) {
        if (hasDefaultConstructor) {
            constructors.clear();
            hasDefaultConstructor = false;
        }
        constructors.add(new DeclaredConstructorReference(this, function));
    }

    public void addMethod(MemberModifiers modifiers, SMethodFunction functionType, String name) {
        methods.add(new DeclaredMethodReference(this, modifiers, name, functionType));
    }

    @Nullable
    public SType getBaseType() {
        return this.baseType;
    }

    public void clearBaseType() {
        this.baseType = null;
    }

    public void setBaseType(SType type) {
        this.baseType = type;
    }

    public SType getActualBaseType() {
        return baseType != null ? baseType : SJavaObject.instance;
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
    public int getLoadInst() {
        return ALOAD;
    }

    @Override
    public int getStoreInst() {
        return ASTORE;
    }

    @Override
    public int getArrayLoadInst() {
        return AALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return AASTORE;
    }

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public int getReturnInst() {
        return ARETURN;
    }

    @Override
    public List<ConstructorReference> getConstructors() {
        return constructors;
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

    @Override
    @Nullable
    public PropertyReference getInstanceProperty(String name) {
        return getInstanceProperties().stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public List<MethodReference> getInstanceMethods() {
        if (baseType != null) {
            List<MethodReference> combined = new ArrayList<>();
            combined.addAll(baseType.getInstanceMethods());
            combined.addAll(methods);
            return combined;
        } else {
            return methods;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}