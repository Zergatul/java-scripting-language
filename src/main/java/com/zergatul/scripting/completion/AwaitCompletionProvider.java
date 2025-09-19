package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundClassMethodNode;
import com.zergatul.scripting.binding.nodes.BoundFunctionNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.NodeType;

import java.util.List;

public class AwaitCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public AwaitCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (!context.canExpression()) {
            return List.of();
        }

        for (CompletionContext current = context; current != null; current = current.up()) {
            if (current.entry == null) {
                break;
            }

            if (current.entry.node.getNodeType() == NodeType.CLASS_CONSTRUCTOR) {
                return List.of();
            }
            if (current.entry.node.getNodeType() == NodeType.CLASS_METHOD) {
                BoundClassMethodNode methodNode = (BoundClassMethodNode) current.entry.node;
                if (methodNode.isAsync) {
                    return List.of(factory.getKeywordSuggestion(TokenType.AWAIT));
                } else {
                    return List.of();
                }
            }
            if (current.entry.node.getNodeType() == NodeType.FUNCTION) {
                BoundFunctionNode functionNode = (BoundFunctionNode) current.entry.node;
                if (functionNode.isAsync) {
                    return List.of(factory.getKeywordSuggestion(TokenType.AWAIT));
                } else {
                    return List.of();
                }
            }
            if (current.entry.node.getNodeType() == NodeType.LAMBDA_EXPRESSION) {
                return List.of();
            }
            if (current.entry.node.getNodeType() == NodeType.STATIC_VARIABLE) {
                return List.of();
            }
            if (current.entry.node.getNodeType() == NodeType.STATEMENTS_LIST) {
                if (parameters.isAsync()) {
                    return List.of(factory.getKeywordSuggestion(TokenType.AWAIT));
                } else {
                    return List.of();
                }
            }
        }

        return List.of();
    }
}