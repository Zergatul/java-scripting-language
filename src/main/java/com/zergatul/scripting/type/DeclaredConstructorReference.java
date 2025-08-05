package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;

public class DeclaredConstructorReference extends ConstructorReference {

    private final SDeclaredType type;

    public DeclaredConstructorReference(SDeclaredType type) {
        this.type = type;
    }

    @Override
    public void compileInvoke(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKESPECIAL,
                Type.getInternalName(type.getJavaClass()),
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE),
                false);
    }

    @Override
    public List<MethodParameter> getParameters() {
        return List.of();
    }
}
