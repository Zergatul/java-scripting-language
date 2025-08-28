package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundCompilationUnitMemberNode;
import com.zergatul.scripting.binding.nodes.BoundFunctionNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.symbols.Function;

import java.util.ArrayList;
import java.util.List;

public class FunctionsCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public FunctionsCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (!context.canExpression()) {
            return List.of();
        }

        List<T> suggestions = new ArrayList<>();
        for (BoundCompilationUnitMemberNode member : output.unit().members.members) {
            if (member instanceof BoundFunctionNode functionNode) {
                String name = functionNode.name.value;
                if (name == null || name.isEmpty()) {
                    continue;
                }

                Function function = functionNode.name.symbolRef.asFunction();
                suggestions.add(factory.getFunctionSuggestion(function));
            }
        }
        return suggestions;
    }
}