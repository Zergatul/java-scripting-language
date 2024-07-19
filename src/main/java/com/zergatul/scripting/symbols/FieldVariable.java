package com.zergatul.scripting.symbols;

import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.compiler.StackHelper;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class FieldVariable extends Variable {

    private final String className;
    private final String fieldName;

    public FieldVariable(SType type, String className, String fieldName) {
        super(null, type, null);
        this.className = className;
        this.fieldName = fieldName;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public void compileStore(CompilerContext context, MethodVisitor visitor) {
        visitor.visitVarInsn(ALOAD, 0);
        StackHelper.swap(visitor, context, getType(), SType.fromJavaType(Object.class));
        visitor.visitFieldInsn(PUTFIELD, className, fieldName, Type.getDescriptor(getType().getJavaClass()));
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitFieldInsn(GETFIELD, className, fieldName, Type.getDescriptor(getType().getJavaClass()));
    }
}