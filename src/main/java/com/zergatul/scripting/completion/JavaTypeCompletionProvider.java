package com.zergatul.scripting.completion;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundJavaTypeNode;
import com.zergatul.scripting.binding.nodes.BoundStaticReferenceExpression;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.JavaInteropPolicy;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.lexer.ValueToken;

import java.util.List;

public class JavaTypeCompletionProvider<T> extends AbstractCompletionProvider<T> {

    private final JavaInteropSuggestionProvider provider;

    public JavaTypeCompletionProvider(SuggestionFactory<T> factory, JavaInteropSuggestionProvider provider) {
        super(factory);
        this.provider = provider;
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        BoundJavaTypeNode javaType = getJavaType(context);
        if (javaType == null) {
            return List.of();
        }

        if (!TextRange.isBetween2(
                context.line,
                context.column,
                javaType.syntaxNode.openBracket,
                javaType.syntaxNode.closeBracket)) {
            return List.of();
        }

        JavaInteropPolicy policy = parameters.getInteropPolicy();
        if (policy != null && !policy.isJavaTypeUsageAllowed()) {
            return List.of();
        }

        String prefix = getPrefix(javaType, context.line, context.column);
        return provider.suggest(prefix).stream()
                .map(factory::getJavaTypeSuggestion)
                .toList();
    }

    private static BoundJavaTypeNode getJavaType(CompletionContext context) {
        if (context.entry == null) {
            return null;
        }
        if (context.entry.node instanceof BoundJavaTypeNode javaType) {
            return javaType;
        }
        if (context.entry.node instanceof BoundStaticReferenceExpression staticReference &&
                staticReference.typeNode instanceof BoundJavaTypeNode javaType) {
            return javaType;
        }
        return null;
    }

    private static String getPrefix(BoundJavaTypeNode javaType, int line, int column) {
        List<Token> tokens = javaType.syntaxNode.name.tokens;
        int lastDotIndex = -1;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.is(TokenType.DOT) && endsAtOrBefore(token, line, column)) {
                lastDotIndex = i;
            }
        }

        if (lastDotIndex < 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lastDotIndex; i++) {
            Token token = tokens.get(i);
            if (token instanceof ValueToken valueToken) {
                builder.append(valueToken.value);
            } else if (token.is(TokenType.DOT)) {
                builder.append('.');
            } else if (token.is(TokenType.DOLLAR)) {
                builder.append('$');
            }
        }
        return builder.toString();
    }

    private static boolean endsAtOrBefore(Token token, int line, int column) {
        TextRange range = token.getRange();
        return range.getLine2() < line ||
                range.getLine2() == line && range.getColumn2() <= column;
    }
}