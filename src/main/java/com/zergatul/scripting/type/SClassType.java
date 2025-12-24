package com.zergatul.scripting.type;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Setter;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;

public class SClassType extends SReferenceType {

    private final Class<?> clazz;

    public SClassType(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<?> getJavaClass() {
        return this.clazz;
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(clazz.getModifiers());
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
    public boolean isInstanceOf(SType other) {
        if (other instanceof SDeclaredType) {
            return false;
        }
        return super.isInstanceOf(other);
    }

    @Override
    public List<ConstructorReference> getConstructors() {
        return Arrays.stream(clazz.getConstructors())
                .map(NativeConstructorReference::new)
                .map(c -> (ConstructorReference) c)
                .toList();
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
    @Nullable
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
                // return Object members only for Object class
                .filter(m -> clazz == Object.class || m.getDeclaringClass() != Object.class)
                .map(NativeInstanceMethodReference::new)
                .map(r -> (MethodReference) r)
                .toList();
    }

    @Override
    public List<MethodReference> getStaticMethods() {
        return Arrays.stream(this.clazz.getMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .filter(m -> m.getDeclaringClass() != Object.class)
                .filter(m -> !m.isAnnotationPresent(Getter.class))
                .filter(m -> !m.isAnnotationPresent(Setter.class))
                .map(NativeStaticMethodReference::new)
                .map(r -> (MethodReference) r)
                .toList();
    }

    @Override
    public List<PropertyReference> getStaticProperties() {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .map(StaticFieldPropertyReference::new)
                .map(r -> (PropertyReference) r)
                .toList();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SClassType other) {
            return other.clazz == clazz;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("Java<%s>", clazz.getName());
    }
}