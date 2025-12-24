package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.type.operation.BinaryOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class ExtensionBinaryOperation extends BinaryOperation {

    private final String internalName;

    public ExtensionBinaryOperation(BinaryOperator operator, SType returnType, SType left, SType right, String internalName) {
        super(operator, returnType, left, right);
        this.internalName = internalName;
    }

    public String getInternalName() {
        return internalName;
    }

    public String getMethodDescriptor() {
        return Type.getMethodDescriptor(getResultType().getAsmType(), getLeft().getAsmType(), getRight().getAsmType());
    }

    @Override
    public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
        right.release(left);
        left.visitMethodInsn(
                INVOKESTATIC,
                context.getClassName(),
                internalName,
                getMethodDescriptor(),
                false);
    }
}