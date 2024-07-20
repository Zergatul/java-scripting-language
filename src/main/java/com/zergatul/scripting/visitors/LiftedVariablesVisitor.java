package com.zergatul.scripting.visitors;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.BoundLambdaExpressionNode;
import com.zergatul.scripting.binding.nodes.BoundVariableDeclarationNode;
import com.zergatul.scripting.symbols.LiftedVariable;

import java.util.ArrayList;
import java.util.List;

public class LiftedVariablesVisitor extends BinderTreeVisitor {

    private final List<LiftedVariable> variables = new ArrayList<>();

    public List<LiftedVariable> getVariables() {
        return variables;
    }

    @Override
    public void explicitVisit(BoundLambdaExpressionNode node) {
        // don't go inside lambdas
    }

    @Override
    public void visit(BoundVariableDeclarationNode node) {
        if (node.name.symbol instanceof LiftedVariable lifted) {
            variables.add(lifted);
        }
    }
}