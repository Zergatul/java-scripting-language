package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundLambdaExpressionNode;
import com.zergatul.scripting.binding.nodes.BoundVariableDeclarationNode;
import com.zergatul.scripting.compiler.AsyncLiftedLocalVariable;

import java.util.ArrayList;
import java.util.List;

public class LiftedVariablesBinderTreeVisitor extends BinderTreeVisitor {

    private final List<AsyncLiftedLocalVariable> variables = new ArrayList<>();

    public List<AsyncLiftedLocalVariable> getVariables() {
        return variables;
    }

    @Override
    public void explicitVisit(BoundLambdaExpressionNode node) {
        // don't go inside lambdas
    }

    @Override
    public void visit(BoundVariableDeclarationNode node) {
        if (node.name.symbol instanceof AsyncLiftedLocalVariable lifted) {
            variables.add(lifted);
        }
    }
}