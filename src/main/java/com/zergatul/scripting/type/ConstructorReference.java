package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;

public class ConstructorReference {

    private final Constructor<?> constructor;

    public ConstructorReference(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    public Optional<String> getDescription() {
        return Optional.empty();
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
            list.add(new MethodParameter(parameters[i].getName(), SType.fromJavaType(types[i])));
        }
        return list;
    }

    public List<SType> getParameterTypes() {
        return getParameters().stream().map(MethodParameter::type).toList();
    }
}