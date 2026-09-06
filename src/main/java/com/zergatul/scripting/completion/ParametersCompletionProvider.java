package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.utility.Lists;

import java.util.ArrayList;
import java.util.List;

public class ParametersCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public ParametersCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (!context.canExpression()) {
            return Lists.of();
        }

        List<T> suggestions = new ArrayList<>();
        for (CompletionContext current = context; current != null; current = current.up()) {
            if (current.entry == null) {
                break;
            }

            List<BoundParameterNode> parameterNodes;
            switch (current.entry.node.getNodeType()) {
                case LAMBDA_EXPRESSION: {
                    BoundLambdaExpressionNode lambda = (BoundLambdaExpressionNode) current.entry.node;
                    parameterNodes = lambda.parameters;
                    break;
                }
                case UNCONVERTED_LAMBDA: {
                    BoundUnconvertedLambdaExpressionNode lambda = (BoundUnconvertedLambdaExpressionNode) current.entry.node;
                    parameterNodes = lambda.parameters;
                    break;
                }
                case CLASS_CONSTRUCTOR: {
                    BoundClassConstructorNode constructor = (BoundClassConstructorNode) current.entry.node;
                    parameterNodes = constructor.parameters.parameters;
                    break;
                }
                case CLASS_METHOD: {
                    BoundClassMethodNode method = (BoundClassMethodNode) current.entry.node;
                    parameterNodes = method.parameters.parameters;
                    break;
                }
                case CLASS_UNARY_OPERATION: {
                    BoundClassUnaryOperationNode operationNode = (BoundClassUnaryOperationNode) current.entry.node;
                    parameterNodes = operationNode.parameters.parameters;
                    break;
                }
                case CLASS_BINARY_OPERATION: {
                    BoundClassBinaryOperationNode operationNode = (BoundClassBinaryOperationNode) current.entry.node;
                    parameterNodes = operationNode.parameters.parameters;
                    break;
                }
                case EXTENSION_METHOD: {
                    BoundExtensionMethodNode method = (BoundExtensionMethodNode) current.entry.node;
                    parameterNodes = method.parameters.parameters;
                    break;
                }
                case EXTENSION_UNARY_OPERATION: {
                    BoundExtensionUnaryOperationNode operationNode = (BoundExtensionUnaryOperationNode) current.entry.node;
                    parameterNodes = operationNode.parameters.parameters;
                    break;
                }
                case EXTENSION_BINARY_OPERATION: {
                    BoundExtensionBinaryOperationNode operationNode = (BoundExtensionBinaryOperationNode) current.entry.node;
                    parameterNodes = operationNode.parameters.parameters;
                    break;
                }
                case FUNCTION_DECLARATION: {
                    BoundFunctionDeclarationNode function = (BoundFunctionDeclarationNode) current.entry.node;
                    parameterNodes = function.parameters.parameters;
                    break;
                }
                default: {
                    parameterNodes = null;
                    break;
                }
            }

            if (parameterNodes != null) {
                for (BoundParameterNode parameter : parameterNodes) {
                    String name = parameter.getName().getSymbol().getName();
                    if (name == null || name.isEmpty()) {
                        continue;
                    }
                    suggestions.add(factory.getLocalVariableSuggestion(parameter.getName().symbolRef.asLocalVariableExpanded()));
                }
            }
        }

        return suggestions;
    }
}