package com.zergatul.scripting.visitors;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.BoundLambdaExpressionNode;

import java.util.ArrayList;
import java.util.List;

public class LambdaVisitor extends BinderTreeVisitor {

    public final List<BoundLambdaExpressionNode> lambdas = new ArrayList<>();

    @Override
    public void explicitVisit(BoundLambdaExpressionNode node) {
        lambdas.add(node);
    }
}