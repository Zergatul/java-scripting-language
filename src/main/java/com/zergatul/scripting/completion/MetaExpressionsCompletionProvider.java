package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.binding.nodes.BoundNodeType;
import com.zergatul.scripting.utility.Lists;

import java.util.List;

public class MetaExpressionsCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public MetaExpressionsCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.canExpression() || insideInvalidMetaExpression(context)) {
            return Lists.of(
                    factory.getKeywordSuggestion(TokenType.META_CAST),
                    factory.getKeywordSuggestion(TokenType.META_TYPE),
                    factory.getKeywordSuggestion(TokenType.META_TYPE_OF));
        } else {
            return Lists.of();
        }
    }

    private boolean insideInvalidMetaExpression(CompletionContext context) {
        return context.entry != null && context.entry.node.getNodeType() == BoundNodeType.META_INVALID_EXPRESSION;
    }
}