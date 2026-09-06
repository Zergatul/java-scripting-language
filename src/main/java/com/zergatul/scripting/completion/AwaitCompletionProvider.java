package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundClassMethodNode;
import com.zergatul.scripting.binding.nodes.BoundFunctionDeclarationNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.binding.nodes.BoundNodeType;
import com.zergatul.scripting.utility.Lists;

import java.util.List;

public class AwaitCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public AwaitCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (!context.canExpression()) {
            return Lists.of();
        }

        for (CompletionContext current = context; current != null; current = current.up()) {
            if (current.entry == null) {
                break;
            }

            if (current.entry.node.getNodeType() == BoundNodeType.CLASS_CONSTRUCTOR) {
                return Lists.of();
            }
            if (current.entry.node.getNodeType() == BoundNodeType.CLASS_METHOD) {
                BoundClassMethodNode methodNode = (BoundClassMethodNode) current.entry.node;
                if (methodNode.isAsync()) {
                    return Lists.of(factory.getKeywordSuggestion(TokenType.AWAIT));
                } else {
                    return Lists.of();
                }
            }
            if (current.entry.node.getNodeType() == BoundNodeType.FUNCTION_DECLARATION) {
                BoundFunctionDeclarationNode functionNode = (BoundFunctionDeclarationNode) current.entry.node;
                if (functionNode.isAsync()) {
                    return Lists.of(factory.getKeywordSuggestion(TokenType.AWAIT));
                } else {
                    return Lists.of();
                }
            }
            if (current.entry.node.getNodeType() == BoundNodeType.LAMBDA_EXPRESSION) {
                return Lists.of();
            }
            if (current.entry.node.getNodeType() == BoundNodeType.STATIC_VARIABLE) {
                return Lists.of();
            }
            if (current.entry.node.getNodeType() == BoundNodeType.STATEMENTS_LIST) {
                if (parameters.isAsync()) {
                    return Lists.of(factory.getKeywordSuggestion(TokenType.AWAIT));
                } else {
                    return Lists.of();
                }
            }
        }

        return Lists.of();
    }
}