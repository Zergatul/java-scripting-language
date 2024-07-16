package com.zergatul.scripting.visitors;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.BoundLambdaExpressionNode;
import com.zergatul.scripting.binding.nodes.BoundVariableDeclarationNode;
import com.zergatul.scripting.symbols.Symbol;

import java.util.ArrayList;
import java.util.List;

public class VariableDeclarationVisitor extends BinderTreeVisitor {

    public final List<Symbol> symbols = new ArrayList<>();

    @Override
    public void explicitVisit(BoundLambdaExpressionNode node) {
        // don't go inside lambda
    }

    @Override
    public void visit(BoundVariableDeclarationNode node) {
        symbols.add(node.name.symbol);
    }
}