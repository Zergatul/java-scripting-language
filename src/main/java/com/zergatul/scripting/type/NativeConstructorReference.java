package com.zergatul.scripting.type;

import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;

public class NativeConstructorReference extends ConstructorReference {

    private final SType owner;
    private final Constructor<?> constructor;
    private final @Nullable GenericSubstitution substitution;

    public NativeConstructorReference(SType owner, Constructor<?> constructor) {
        this.owner = owner;
        this.constructor = constructor;
        this.substitution = null;
    }

    public NativeConstructorReference(SType owner, Constructor<?> constructor, GenericSubstitution substitution) {
        this.owner = owner;
        this.constructor = constructor;
        this.substitution = substitution;
    }

    @Override
    public SType getOwner() {
        return owner;
    }

    @Override
    public Visibility getVisibility() {
        if (Modifier.isPublic(constructor.getModifiers())) {
            return Visibility.PUBLIC;
        } else if (Modifier.isProtected(constructor.getModifiers())) {
            return Visibility.PROTECTED;
        } else {
            return Visibility.PRIVATE;
        }
    }

    public void compileInvoke(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKESPECIAL,
                Type.getInternalName(constructor.getDeclaringClass()),
                "<init>",
                Type.getConstructorDescriptor(constructor),
                false);
    }

    public List<MethodParameter> getParameters() {
        Parameter[] parameters = constructor.getParameters();
        java.lang.reflect.Type[] types = constructor.getGenericParameterTypes();
        List<MethodParameter> list = new ArrayList<>(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            SType type;
            if (substitution == null) {
                type = SType.fromJavaType(types[i]);
            } else {
                type = substitution.resolveType(types[i]);
            }
            list.add(new MethodParameter(parameters[i].getName(), type));
        }
        return list;
    }

    public List<SType> getParameterTypes() {
        return getParameters().stream().map(MethodParameter::type).toList();
    }
}