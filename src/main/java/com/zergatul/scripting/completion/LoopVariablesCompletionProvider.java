package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundForEachLoopStatementNode;
import com.zergatul.scripting.binding.nodes.BoundForLoopStatementNode;
import com.zergatul.scripting.binding.nodes.BoundVariableDeclarationNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.symbols.LocalVariable;

import java.util.ArrayList;
import java.util.List;

public class LoopVariablesCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public LoopVariablesCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (!context.canExpression()) {
            return List.of();
        }

        List<T> suggestions = new ArrayList<>();
        while (context != null && context.entry != null) {
            switch (context.entry.node.getNodeType()) {
                case FOR_LOOP_STATEMENT -> {
                    BoundForLoopStatementNode loop = (BoundForLoopStatementNode) context.entry.node;
                    if (loop.init instanceof BoundVariableDeclarationNode declaration) {
                        if (declaration.name.getSymbol() instanceof LocalVariable local) {
                            if (local.getName() == null || local.getName().isEmpty()) {
                                continue;
                            }
                            suggestions.add(factory.getLocalVariableSuggestion(local));
                        }
                    }
                }
                case FOREACH_LOOP_STATEMENT -> {
                    BoundForEachLoopStatementNode loop = (BoundForEachLoopStatementNode) context.entry.node;
                    if (loop.name.getSymbol() instanceof LocalVariable local) {
                        if (local.getName() == null || local.getName().isEmpty()) {
                            continue;
                        }
                        suggestions.add(factory.getLocalVariableSuggestion(local));
                    }
                }
            }
            context = context.up();
        }

        return suggestions;
    }
}