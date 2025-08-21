package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.compiler.CompilationParameters;

import java.util.ArrayList;
import java.util.List;

public class ParametersCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public ParametersCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (!context.canExpression()) {
            return List.of();
        }


        List<T> suggestions = new ArrayList<>();
        for (CompletionContext current = context; current != null; current = current.up()) {
            if (current.entry == null) {
                break;
            }

            List<BoundParameterNode> parameterNodes = switch (current.entry.node.getNodeType()) {
                case LAMBDA_EXPRESSION -> {
                    BoundLambdaExpressionNode lambda = (BoundLambdaExpressionNode) current.entry.node;
                    yield lambda.parameters;
                }
                case CLASS_CONSTRUCTOR -> {
                    BoundClassConstructorNode constructor = (BoundClassConstructorNode) current.entry.node;
                    yield constructor.parameters.parameters;
                }
                case CLASS_METHOD -> {
                    BoundClassMethodNode method = (BoundClassMethodNode) current.entry.node;
                    yield method.parameters.parameters;
                }
                case FUNCTION -> {
                    BoundFunctionNode function = (BoundFunctionNode) current.entry.node;
                    yield function.parameters.parameters;
                }
                default -> null;
            };

            if (parameterNodes != null) {
                for (BoundParameterNode parameter : parameterNodes) {
                    String name = parameter.getName().getSymbol().getName();
                    if (name == null || name.isEmpty()) {
                        continue;
                    }
                    suggestions.add(factory.getLocalVariableSuggestion(parameter.getName().symbolRef.asLocalVariable()));
                }
            }
        }

        return suggestions;
    }
}