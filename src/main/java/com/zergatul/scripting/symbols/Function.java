package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.type.Invocable;
import com.zergatul.scripting.type.MethodParameter;
import com.zergatul.scripting.type.SStaticFunction;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class Function extends Symbol implements Invocable {

    public Function(String name, SStaticFunction type, TextRange definition) {
        super(name, type, definition);
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        throw new InternalException();
    }

    public SStaticFunction getFunctionType() {
        return (SStaticFunction) getType();
    }

    @Override
    public List<MethodParameter> getParameters() {
        return getFunctionType().getParameters();
    }

    @Override
    public String toDiagnosticsString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getFunctionType().getReturnType());
        sb.append(' ');
        sb.append(getName());
        sb.append('(');
        List<MethodParameter> parameters = getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            MethodParameter parameter = parameters.get(i);
            sb.append(parameter.type());
            sb.append(' ');
            sb.append(parameter.name());
            if (i < parameters.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(')');
        return sb.toString();
    }
}