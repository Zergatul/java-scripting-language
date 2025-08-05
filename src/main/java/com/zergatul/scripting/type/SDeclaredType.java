package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SDeclaredType extends SType {

    private final String name;
    private final List<Field> fields = new ArrayList<>();
    private Class<?> clazz;

    public SDeclaredType(String name) {
        this.name = name;
    }

    public void setJavaClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    public void addField(SType type, String name) {
        fields.add(new Field(type, name));
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
        return List.of(new DeclaredConstructorReference(this));
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