package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.PUTSTATIC;

public class DeclaredStaticVariable extends StaticVariable {

    public DeclaredStaticVariable(String name, SType type, TextRange definition) {
        super(name, type, definition);
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean canSet() {
        return true;
    }

    @Override
    public void compileStore(CompilerContext context, MethodVisitor visitor) {
        if (context.getClassName() == null) {
            throw new InternalException("Class name is not set in CompilerContext.");
        }

        visitor.visitFieldInsn(
                PUTSTATIC,
                context.getClassName(),
                getName(),
                Type.getDescriptor(getType().getJavaClass()));
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        if (context.getClassName() == null) {
            throw new InternalException("Class name is not set in CompilerContext.");
        }

        visitor.visitFieldInsn(
                GETSTATIC,
                context.getClassName(),
                getName(),
                Type.getDescriptor(getType().getJavaClass()));
    }
}
