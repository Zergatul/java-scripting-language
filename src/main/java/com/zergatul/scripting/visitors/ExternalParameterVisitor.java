package com.zergatul.scripting.visitors;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;
import com.zergatul.scripting.symbols.*;

import java.util.ArrayList;
import java.util.List;

public class ExternalParameterVisitor extends BinderTreeVisitor {

    private final List<Variable> parameters = new ArrayList<>();

    public List<Variable> getParameters() {
        return parameters;
    }

    @Override
    public void visit(BoundNameExpressionNode node) {
        if (node.symbol instanceof Variable local) {
            Variable variable = local;
            while (variable instanceof CapturedVariable captured) {
                variable = captured.getUnderlying();
            }
            if (variable instanceof LiftedVariable lifted) {
                if (lifted.getUnderlying() instanceof ExternalParameter) {
                    if (!parameters.contains(lifted)) {
                        parameters.add(lifted);
                    }
                }
            }
            if (variable instanceof ExternalParameter parameter) {
                if (!parameters.contains(parameter)) {
                    parameters.add(parameter);
                }
            }
        }
    }
}