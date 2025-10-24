package com.zergatul.scripting.completion;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.JavaInteropPolicy;
import com.zergatul.scripting.parser.nodes.InvocationExpressionNode;
import com.zergatul.scripting.parser.nodes.MemberAccessExpressionNode;
import com.zergatul.scripting.parser.nodes.ParserNodeType;
import com.zergatul.scripting.type.NativeMethodReference;
import com.zergatul.scripting.type.SType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BaseMethodsCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public BaseMethodsCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.entry == null) {
            return List.of();
        }

        if (context.entry.node.is(BoundNodeType.PROPERTY_ACCESS_EXPRESSION)) {
            BoundPropertyAccessExpressionNode propertyAccessNode = (BoundPropertyAccessExpressionNode) context.entry.node;
            if (propertyAccessNode.syntaxNode.callee.is(ParserNodeType.BASE_EXPRESSION)) {
                if (propertyAccessNode.syntaxNode.dot.getRange().isBefore(context.line, context.column)) {
                    return getBaseClassMethodSuggestions(parameters, context);
                }
            }
        }

        if (context.entry.node.is(BoundNodeType.INVALID_EXPRESSION)) {
            BoundInvalidExpressionNode invalidExpressionNode = (BoundInvalidExpressionNode) context.entry.node;
            boolean isBaseMethodInvocation =
                    invalidExpressionNode.unboundNodes.size() == 1 &&
                    invalidExpressionNode.unboundNodes.getFirst() instanceof InvocationExpressionNode invocationNode &&
                    invocationNode.callee instanceof MemberAccessExpressionNode memberAccessNode &&
                    memberAccessNode.callee.is(ParserNodeType.BASE_EXPRESSION);
            if (isBaseMethodInvocation) {
                InvocationExpressionNode invocationNode = (InvocationExpressionNode) invalidExpressionNode.unboundNodes.getFirst();
                MemberAccessExpressionNode memberAccessNode = (MemberAccessExpressionNode) invocationNode.callee;
                if (TextRange.isBetween(context.line, context.column, memberAccessNode.dot, invocationNode.arguments)) {
                    return getBaseClassMethodSuggestions(parameters, context);
                }
            }
        }

        if (context.entry.node.is(BoundNodeType.METHOD)) {
            if (context.entry.parent != null && context.entry.parent.node.is(BoundNodeType.BASE_METHOD_INVOCATION_EXPRESSION)) {
                return getBaseClassMethodSuggestions(parameters, context);
            }
        }

        return List.of();
    }

    private List<T> getBaseClassMethodSuggestions(CompilationParameters parameters, CompletionContext context) {
        BoundClassNode classNode = findClassNode(context);
        if (classNode != null) {
            List<T> suggestions = new ArrayList<>();
            SType baseType = classNode.getDeclaredType().getActualBaseType();
            baseType.getInstanceMethods().stream()
                    .filter(m -> {
                        if (m instanceof NativeMethodReference nativeRef) {
                            JavaInteropPolicy checker = parameters.getPolicy();
                            if (checker != null) {
                                return checker.isMethodVisible(nativeRef.getUnderlying());
                            } else {
                                return true;
                            }
                        } else {
                            return true;
                        }
                    })
                    .forEach(m -> suggestions.add(factory.getMethodSuggestion(m)));
            return suggestions;
        } else {
            return List.of();
        }
    }

    @Nullable
    private static BoundClassNode findClassNode(CompletionContext context) {
        SearchEntry entry = context.entry;
        while (entry != null) {
            if (entry.node.is(BoundNodeType.CLASS_DECLARATION)) {
                return (BoundClassNode) entry.node;
            }
            entry = entry.parent;
        }
        return null;
    }
}