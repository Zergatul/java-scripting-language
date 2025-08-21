package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundIfStatementNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.TokenType;

import java.util.List;

public class ElseKeywordCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public ElseKeywordCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.entry == null) {
            return List.of();
        }

        switch (context.entry.node.getNodeType()) {
            case STATEMENTS_LIST, BLOCK_STATEMENT -> {
                if (context.prev instanceof BoundIfStatementNode ifStatement && ifStatement.elseStatement == null) {
                    return List.of(factory.getKeywordSuggestion(TokenType.ELSE));
                }
            }
        }

        return List.of();
    }
}