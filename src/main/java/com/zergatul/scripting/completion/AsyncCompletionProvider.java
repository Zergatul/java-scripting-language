package com.zergatul.scripting.completion;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundExtensionNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.binding.nodes.BoundNodeType;

import java.util.List;

public class AsyncCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public AsyncCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.canUnitMember()) {
            return List.of(factory.getKeywordSuggestion(TokenType.ASYNC));
        }
        if (context.entry != null && context.entry.node.getNodeType() == BoundNodeType.EXTENSION_DECLARATION) {
            BoundExtensionNode extension = (BoundExtensionNode) context.entry.node;
            if (TextRange.isBetween(context.line, context.column, extension.syntaxNode.openBrace, extension.syntaxNode.closeBrace)) {
                return List.of(factory.getKeywordSuggestion(TokenType.ASYNC));
            } else {
                return List.of();
            }
        }
        return List.of();
    }
}