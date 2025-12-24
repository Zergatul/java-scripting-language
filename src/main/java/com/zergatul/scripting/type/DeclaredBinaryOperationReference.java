package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class DeclaredBinaryOperationReference extends MethodReference {

    private final SDeclaredType type;
    private final BinaryOperator operator;
    private final SMethodFunction functionType;

    public DeclaredBinaryOperationReference(SDeclaredType type, BinaryOperator operator, SMethodFunction functionType) {
        this.type = type;
        this.operator = operator;
        this.functionType = functionType;
    }

    @Override
    public SType getOwner() {
        return type;
    }

    @Override
    public SType getReturn() {
        return functionType.getReturnType();
    }

    @Override
    public List<MethodParameter> getParameters() {
        return functionType.getParameters();
    }

    @Override
    public void compileInvoke(MethodVisitor visitor, CompilerContext context) {
        throw new InternalException();
    }

    @Override
    public String getName() {
        return "op_" + operator.name().toLowerCase();
    }

    public BinaryOperator getOperator() {
        return operator;
    }
}
