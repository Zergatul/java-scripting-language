package com.zergatul.scripting.visitors;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;
import com.zergatul.scripting.symbols.*;

import java.util.ArrayList;
import java.util.List;

public class LocalParameterVisitor extends BinderTreeVisitor {

    private final List<LiftedVariable> parameters = new ArrayList<>();

    public List<LiftedVariable> getParameters() {
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
                if (lifted.getUnderlying() instanceof LocalParameter) {
                    if (!parameters.contains(lifted)) {
                        parameters.add(lifted);
                    }
                }
            }
        }
    }
}
