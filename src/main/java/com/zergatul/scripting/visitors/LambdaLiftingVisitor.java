package com.zergatul.scripting.visitors;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.BoundLambdaExpressionNode;
import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;
import com.zergatul.scripting.binding.nodes.BoundVariableDeclarationNode;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.symbols.CapturedLocalVariable;
import com.zergatul.scripting.symbols.LambdaLiftedLocalVariable;
import com.zergatul.scripting.symbols.LocalParameter;
import com.zergatul.scripting.symbols.LocalVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LambdaLiftingVisitor extends BinderTreeVisitor {

    private final Stack<Entry> stack = new Stack<>();

    @Override
    public void explicitVisit(BoundLambdaExpressionNode node) {
        stack.push(new Entry(node));
        super.explicitVisit(node);
        stack.pop();
    }

    @Override
    public void visit(BoundVariableDeclarationNode node) {
        if (stack.isEmpty()) {
            return;
        }
        if (node.name.symbol instanceof LocalVariable variable) {
            stack.peek().locals.add(variable);
        }
    }

    @Override
    public void visit(BoundNameExpressionNode node) {
        if (stack.isEmpty()) {
            return;
        }
        if (node.symbol instanceof LocalParameter) {
            return;
        }
        if (node.symbol instanceof LocalVariable variable) {
            if (stack.peek().locals.contains(variable)) {
                return;
            }
            // variable from another scope
            CompilerContext context = variable.getFunctionContext();
            LambdaLiftedLocalVariable lifted = new LambdaLiftedLocalVariable(variable);
            for (BoundNameExpressionNode name : variable.getReferences()) {
                if (name)
            }

            for (int i = stack.size() - 1; i >= 0; i--) {
                if (stack.get(i).locals.contains(variable)) {
                    stack.peek();
                }
            }
        }
    }

    private static class Entry {

        public final BoundLambdaExpressionNode lambda;
        public final List<LocalVariable> locals;

        public Entry(BoundLambdaExpressionNode lambda) {
            this.lambda = lambda;
            this.locals = new ArrayList<>();
        }
    }
}