package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.type.Invocable;
import com.zergatul.scripting.type.MethodParameter;
import com.zergatul.scripting.type.SFunction;
import com.zergatul.scripting.type.SUnknown;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class InvocableVariable extends Variable implements Invocable {

    public static final InvocableVariable unknown = new InvocableVariable();

    private final Variable variable;

    public InvocableVariable(Variable variable) {
        super(variable.getName(), variable.getType(), variable.getDefinition());
        this.variable = variable;
    }

    private InvocableVariable() {
        super("", SUnknown.instance, null);
        this.variable = null;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public void compileStore(CompilerContext context, MethodVisitor visitor) {
        throw new InternalException();
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        throw new InternalException();
    }

    @Override
    public List<MethodParameter> getParameters() {
        return ((SFunction) variable.getType()).getParameters();
    }
}