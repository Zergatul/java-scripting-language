package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundCompilationUnitMemberNode;
import com.zergatul.scripting.binding.nodes.BoundFunctionNode;
import com.zergatul.scripting.binding.nodes.BoundStaticVariableNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.symbols.DeclaredStaticVariable;
import com.zergatul.scripting.symbols.Function;

import java.util.ArrayList;
import java.util.List;

public class StaticFieldsCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public StaticFieldsCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.canExpression()) {
            List<T> suggestions = new ArrayList<>();
            for (BoundCompilationUnitMemberNode member : output.unit().members.members) {
                if (member instanceof BoundStaticVariableNode staticVariableNode) {
                    String name = staticVariableNode.name.value;
                    if (name == null || name.isEmpty()) {
                        continue;
                    }

                    DeclaredStaticVariable variable = staticVariableNode.name.symbolRef.asStaticVariable();
                    suggestions.add(factory.getStaticFieldSuggestion(variable));
                }
            }
            return suggestions;
        } else {
            return List.of();
        }
    }
}