package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundNode;
import com.zergatul.scripting.binding.nodes.BoundStatementNode;
import com.zergatul.scripting.binding.nodes.BoundVariableDeclarationNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.symbols.LocalVariable;

import java.util.ArrayList;
import java.util.List;

public class LocalVariablesCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public LocalVariablesCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (!context.canExpression()) {
            return List.of();
        }

        context = context.closestStatement();
        if (context == null) {
            return List.of();
        }

        List<T> suggestions = new ArrayList<>();
        for (BoundStatementNode statement : getStatementsPriorTo(context)) {
            if (statement instanceof BoundVariableDeclarationNode declaration) {
                // can be lifted variable?
                if (declaration.name.getSymbol() instanceof LocalVariable local) {
                    if (local.getName() == null || local.getName().isEmpty()) {
                        continue;
                    }
                    suggestions.add(factory.getLocalVariableSuggestion(local));
                }
            }
        }
        return suggestions;
    }

    private List<BoundStatementNode> getStatementsPriorTo(CompletionContext context) {
        if (context.entry == null) {
            return List.of();
        }

        CompletionContext parent = context.up();
        if (parent == null || parent.entry == null) {
            return List.of();
        }

        if (parent.entry.node instanceof BoundStatementNode) {
            List<BoundStatementNode> statements = new ArrayList<>();
            for (BoundNode child : parent.entry.node.getChildren()) {
                if (child == context.entry.node) {
                    break;
                }
                if (child instanceof BoundStatementNode statement) {
                    statements.add(statement);
                }
            }
            return statements;
        }

        return List.of();
    }
}