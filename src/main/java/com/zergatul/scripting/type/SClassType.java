package com.zergatul.scripting.type;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Setter;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.*;
import java.util.*;

public class SClassType extends SReferenceType {

    private final Class<?> clazz;

    protected SClassType(Class<?> clazz) {
        this.clazz = clazz;
    }

    public static SClassType create(Class<?> clazz) {
        if (clazz == Object.class) {
            return SJavaObject.instance;
        } else {
            return new SClassType(clazz);
        }
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
    public @Nullable SType getBaseType() {
        if (clazz.isInterface()) {
            return null;
        } else {
            return SType.fromJavaType(clazz.getSuperclass());
        }
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
        List<PropertyReference> properties = new ArrayList<>();
        properties.addAll(Arrays.stream(clazz.getFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .map(FieldPropertyReference::new)
                .map(f -> (PropertyReference) f)
                .toList());
        properties.addAll(Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .filter(f -> Modifier.isPrivate(f.getModifiers()))
                .map(FieldPropertyReference::new)
                .map(f -> (PropertyReference) f)
                .toList());
        return properties;
    }

    @Override
    public List<MethodReference> getInstanceMethods() {
        Set<Method> methods = new HashSet<>();
        for (Method method : this.clazz.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            methods.add(method);
        }
        for (Method method : this.clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            methods.add(method);
        }
        return methods.stream().map(m -> (MethodReference) new NativeMethodReference(m)).toList();
    }

    @Override
    public List<MethodReference> getDeclaredInstanceMethods() {
        return Arrays.stream(this.clazz.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .map(NativeMethodReference::new)
                .map(r -> (MethodReference) r)
                .toList();
    }

    @Override
    public List<PropertyReference> getStaticProperties() {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .map(FieldPropertyReference::new)
                .map(r -> (PropertyReference) r)
                .toList();
    }

    @Override
    public List<MethodReference> getStaticMethods() {
        return Arrays.stream(this.clazz.getDeclaredMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .filter(m -> !m.isAnnotationPresent(Getter.class))
                .filter(m -> !m.isAnnotationPresent(Setter.class))
                .map(NativeMethodReference::new)
                .map(r -> (MethodReference) r)
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