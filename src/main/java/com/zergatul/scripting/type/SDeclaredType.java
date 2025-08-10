package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SDeclaredType extends SType {

    private final String name;
    private final List<Field> fields = new ArrayList<>();
    private final List<SFunction> constructors = new ArrayList<>();
    private String internalName;
    private Class<?> clazz;

    public SDeclaredType(String name) {
        this.name = name;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public void setJavaClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    public void addField(SType type, String name) {
        fields.add(new Field(type, name));
    }

    public void addConstructor(SFunction function) {
        constructors.add(function);
    }

    @Override
    public boolean isSyntheticType() {
        return true;
    }

    @Override
    public boolean isInstanceOf(SType other) {
        if (equals(other)) {
            return true;
        }
        return !other.isSyntheticType() && other.getJavaClass() == Object.class;
    }

    @Override
    public String getInternalName() {
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
        if (constructors.isEmpty()) {
            return List.of(new DeclaredConstructorReference(this));
        } else {
            return constructors.stream()
                    .map(f -> new DeclaredConstructorReference(this, f))
                    .map(c -> (ConstructorReference) c)
                    .toList();
        }
    }

    @Override
    public List<PropertyReference> getInstanceProperties() {
        return fields.stream()
                .map(f -> new DeclaredFieldReference(this, f.type, f.name))
                .map(f -> (PropertyReference) f)
                .toList();
    }

    @Override
    public PropertyReference getInstanceProperty(String name) {
        return getInstanceProperties().stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return name;
    }

    private record Field(SType type, String name) {}
}