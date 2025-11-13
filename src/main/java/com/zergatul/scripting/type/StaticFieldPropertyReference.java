package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.objectweb.asm.Opcodes.GETSTATIC;

public class StaticFieldPropertyReference extends PropertyReference {

    private final Field field;

    public StaticFieldPropertyReference(Field field) {
        this.field = field;
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public SType getType() {
        return SType.fromJavaType(field.getType());
    }

    @Override
    public boolean canGet() {
        return true;
    }

    @Override
    public boolean canSet() {
        return !Modifier.isFinal(field.getModifiers());
    }

    @Override
    public void compileGet(MethodVisitor visitor) {
        visitor.visitFieldInsn(
                GETSTATIC,
                Type.getInternalName(field.getDeclaringClass()),
                field.getName(),
                Type.getDescriptor(field.getType()));
    }
}