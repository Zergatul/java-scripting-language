package com.zergatul.scripting.symbols;

import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.compiler.StackHelper;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.PUTFIELD;

public class CapturedVariable extends Variable {

    private final Variable variable;
    private final SymbolRef closure;

    public CapturedVariable(Variable variable) {
        super(variable.getName(), variable.getType(), variable.getDefinition());
        this.variable = variable;
        this.closure = new ForwardSymbolRef();
    }

    public Variable getUnderlying() {
        return variable;
    }

    @Override
    public boolean canSet() {
        return variable.canSet();
    }

    @Override
    public boolean isConstant() {
        return variable.isConstant();
    }

    @Override
    public void compileStore(CompilerContext context, MethodVisitor visitor) {
        closure.get().compileLoad(context, visitor);
        StackHelper.swap(visitor, context, variable.getType(), closure.get().getType());
        visitor.visitFieldInsn(PUTFIELD, getClosureClassName(), getClosureFieldName(), Type.getDescriptor(variable.getType().getJavaClass()));
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        closure.get().compileLoad(context, visitor);
        visitor.visitFieldInsn(GETFIELD, getClosureClassName(), getClosureFieldName(), Type.getDescriptor(variable.getType().getJavaClass()));
    }

    public SymbolRef getClosure() {
        return closure;
    }

    public String getClosureClassName() {
        return getOriginal().getClassName();
    }

    public String getClosureFieldName() {
        return getOriginal().getFieldName();
    }

    private LiftedVariable getOriginal() {
        Variable current = variable;
        while (current instanceof CapturedVariable captured) {
            current = captured.variable;
        }
        return (LiftedVariable) current;
    }
}