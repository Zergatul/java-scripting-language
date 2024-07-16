package com.zergatul.scripting.visitors;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.BoundLambdaExpressionNode;
import com.zergatul.scripting.binding.nodes.BoundVariableDeclarationNode;
import com.zergatul.scripting.symbols.AsyncLiftedLocalVariable;

import java.util.ArrayList;
import java.util.List;

public class LiftedVariablesVisitor extends BinderTreeVisitor {

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