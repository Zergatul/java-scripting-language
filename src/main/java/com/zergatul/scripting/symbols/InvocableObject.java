package com.zergatul.scripting.symbols;

import com.zergatul.scripting.type.Invocable;
import com.zergatul.scripting.type.MethodParameter;
import com.zergatul.scripting.type.SFunction;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class InvocableObject implements Invocable {

    private final SFunction function;

    public InvocableObject(SFunction function) {
        this.function = function;
    }

    @Override
    public List<MethodParameter> getParameters() {
        return function.getParameters();
    }

    public SType getReturnType() {
        return function.getReturnType();
    }

    @Override
    public String toDiagnosticsString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getReturnType());
        sb.append(' ');
        sb.append("<invocable>");
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