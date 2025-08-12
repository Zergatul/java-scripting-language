package com.zergatul.scripting.visitors;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;
import com.zergatul.scripting.symbols.*;

import java.util.ArrayList;
import java.util.List;

public class ExternalParameterVisitor extends BinderTreeVisitor {

    private final List<SymbolRef> parameters = new ArrayList<>();

    public List<SymbolRef> getParameters() {
        return parameters;
    }

    @Override
    public void visit(BoundNameExpressionNode node) {
        if (node.getSymbol() instanceof Variable local) {
            Variable variable = local;
            while (variable instanceof CapturedVariable captured) {
                variable = captured.getUnderlying();
            }
            if (variable instanceof LiftedVariable lifted) {
                if (lifted.getUnderlying() instanceof ExternalParameter) {
                    if (!parameters.contains(node.symbolRef)) {
                        parameters.add(node.symbolRef);
                    }
                }
            }
            if (variable instanceof ExternalParameter parameter) {
                if (!parameters.contains(node.symbolRef)) {
                    parameters.add(node.symbolRef);
                }
            }
        }
    }
}