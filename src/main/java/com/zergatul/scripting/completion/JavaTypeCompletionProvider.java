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
import com.zergatul.scripting.utility.Lists;

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
            return Lists.of();
        }

        if (!TextRange.isBetween2(
                context.line,
                context.column,
                javaType.syntaxNode.openBracket,
                javaType.syntaxNode.closeBracket)) {
            return Lists.of();
        }

        JavaInteropPolicy policy = parameters.getInteropPolicy();
        if (policy != null && !policy.isJavaTypeUsageAllowed()) {
            return Lists.of();
        }

        String prefix = getPrefix(javaType, context.line, context.column);
        return Lists.from(
                provider.suggest(prefix).stream()
                        .map(factory::getJavaTypeSuggestion));
    }

    private static BoundJavaTypeNode getJavaType(CompletionContext context) {
        if (context.entry == null) {
            return null;
        }
        if (context.entry.node instanceof BoundJavaTypeNode) {
            return (BoundJavaTypeNode) context.entry.node;
        }
        if (context.entry.node instanceof BoundStaticReferenceExpression && ((BoundStaticReferenceExpression) context.entry.node).typeNode instanceof BoundJavaTypeNode) {
            return (BoundJavaTypeNode) ((BoundStaticReferenceExpression) context.entry.node).typeNode;
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
            if (token instanceof ValueToken) {
                ValueToken valueToken = (ValueToken) token;
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