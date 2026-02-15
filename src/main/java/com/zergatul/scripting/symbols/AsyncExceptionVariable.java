package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.CompilerContext;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;

public class AsyncExceptionVariable extends Variable {

    public AsyncExceptionVariable(Variable original) {
        super(original.getName(), original.getType(), original.getDefinition());
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitFieldInsn(
                GETFIELD,
                context.getAsyncStateMachineClassName(),
                "exception",
                Type.getDescriptor(Throwable.class));
    }

    @Override
    public void compileStore(CompilerContext context, MethodVisitor visitor) {
        throw new InternalException();
    }
}