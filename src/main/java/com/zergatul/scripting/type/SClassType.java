package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SClassType extends SType {

    private final Class<?> clazz;

    public SClassType(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<?> getJavaClass() {
        return this.clazz;
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
    public List<PropertyReference> getInstanceProperties() {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .map(FieldPropertyReference::new)
                .map(f -> (PropertyReference) f)
                .toList();
    }

    @Override
    public PropertyReference getInstanceProperty(String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            if (Modifier.isStatic(field.getModifiers())) {
                return null;
            }
            if (!Modifier.isPublic(field.getModifiers())) {
                return null;
            }
            return new FieldPropertyReference(field);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    @Override
    public List<MethodReference> getInstanceMethods() {
        return Arrays.stream(this.clazz.getMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .filter(m -> m.getDeclaringClass() != Object.class)
                .map(NativeMethodReference::new)
                .map(r -> (MethodReference) r)
                .toList();
    }

    @Override
    public String toString() {
        return String.format("Java<%s>", clazz.getName());
    }
}