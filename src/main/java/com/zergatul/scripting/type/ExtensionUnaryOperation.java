package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class ExtensionUnaryOperation extends UnaryOperation {

    private final String internalName;

    public ExtensionUnaryOperation(UnaryOperator operator, SType returnType, SType operandType, String internalName) {
        super(operator, returnType, operandType);
        this.internalName = internalName;
    }

    public String getInternalName() {
        return internalName;
    }

    public String getMethodDescriptor() {
        return Type.getMethodDescriptor(getResultType().getAsmType(), getOperandType().getAsmType());
    }

    @Override
    public void apply(MethodVisitor visitor, CompilerContext context) {
        visitor.visitMethodInsn(
                INVOKESTATIC,
                context.getClassName(),
                internalName,
                getMethodDescriptor(),
                false);
    }
}