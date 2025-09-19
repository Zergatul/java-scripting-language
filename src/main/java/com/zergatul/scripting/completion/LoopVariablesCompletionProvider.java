package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundForEachLoopStatementNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.NodeType;
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
            if (context.entry.node.getNodeType() == NodeType.FOREACH_LOOP_STATEMENT) {
                BoundForEachLoopStatementNode loop = (BoundForEachLoopStatementNode) context.entry.node;
                if (loop.name.getSymbol() instanceof LocalVariable local) {
                    if (local.getName() != null && !local.getName().isEmpty()) {
                        suggestions.add(factory.getLocalVariableSuggestion(local));
                    }
                }
            }
            context = context.up();
        }

        return suggestions;
    }
}