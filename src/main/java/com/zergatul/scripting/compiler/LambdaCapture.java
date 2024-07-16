package com.zergatul.scripting.compiler;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.symbols.Symbol;
import com.zergatul.scripting.visitors.LambdaVisitor;
import com.zergatul.scripting.binding.nodes.BoundLambdaExpressionNode;
import com.zergatul.scripting.binding.nodes.BoundStatementNode;
import com.zergatul.scripting.visitors.VariableDeclarationVisitor;

import java.util.List;

public class LambdaCapture {

    public void process(List<BoundStatementNode> statements) {
        LambdaVisitor visitor = new LambdaVisitor();
        for (BoundStatementNode statement : statements) {
            statement.accept(visitor);
        }

        for (BoundLambdaExpressionNode lambda : visitor.lambdas) {
            process(lambda);
        }
    }

    private void process(BoundLambdaExpressionNode node) {
        // process inner lambdas
        process(List.of(node.body));

        VariableDeclarationVisitor visitor = new VariableDeclarationVisitor();
        node.body.accept(visitor);
        List<Symbol> declarations = visitor.symbols;

        node.body.accept(new BinderTreeVisitor() {
        });
    }
}