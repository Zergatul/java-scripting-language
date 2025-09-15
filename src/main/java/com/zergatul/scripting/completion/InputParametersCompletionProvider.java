package com.zergatul.scripting.completion;

import com.zergatul.scripting.InterfaceHelper;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundClassMethodNode;
import com.zergatul.scripting.binding.nodes.BoundFunctionNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class InputParametersCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public InputParametersCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (!context.canExpression()) {
            return List.of();
        }

        Method method = InterfaceHelper.getFuncInterfaceMethod(parameters.getFunctionalInterface());
        if (method.getParameters().length == 0) {
            return List.of();
        }

        if (context.type == ContextType.NO_CODE || context.type == ContextType.AFTER_LAST_WITH_STATEMENTS || context.type == ContextType.AFTER_LAST_NO_STATEMENTS) {
            return getSuggestions(method);
        }

        for (CompletionContext current = context; current != null; current = current.up()) {
            if (current.entry == null) {
                break;
            }

            switch (current.entry.node.getNodeType()) {
                case COMPILATION_UNIT_MEMBERS -> {
                    return List.of();
                }
                case STATEMENTS_LIST -> {
                    return getSuggestions(method);
                }
            }
        }

        return List.of();
    }

    private List<T> getSuggestions(Method method) {
        Parameter[] methodParameters = method.getParameters();
        Type[] methodParameterTypes = method.getGenericParameterTypes();
        List<T> suggestions = new ArrayList<>(methodParameters.length);
        for (int i = 0; i < methodParameters.length; i++) {
            suggestions.add(factory.getInputParameterSuggestion(methodParameters[i].getName(), SType.fromJavaType(methodParameterTypes[i])));
        }
        return suggestions;
    }
}
