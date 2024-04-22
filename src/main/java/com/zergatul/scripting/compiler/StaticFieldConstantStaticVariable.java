package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.objectweb.asm.Opcodes.GETSTATIC;

public class StaticFieldConstantStaticVariable extends ConstantStaticVariable {

    private final Field field;

    protected StaticFieldConstantStaticVariable(String name, Field field) {
        super(name, SType.fromJavaType(field.getType()));

        if (!Modifier.isStatic(field.getModifiers())) {
            throw new InternalException();
        }
        if (!Modifier.isPublic(field.getModifiers())) {
            throw new InternalException();
        }

        this.field = field;
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        visitor.visitFieldInsn(
                GETSTATIC,
                Type.getInternalName(field.getDeclaringClass()),
                field.getName(),
                Type.getDescriptor(field.getType()));
    }
}
