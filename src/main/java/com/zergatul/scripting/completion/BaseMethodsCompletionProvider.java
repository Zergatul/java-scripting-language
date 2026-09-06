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
import com.zergatul.scripting.type.MemberLookup;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.Visibility;
import com.zergatul.scripting.utility.Lists;
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
            return Lists.of();
        }

        if (context.entry.node.is(BoundNodeType.PROPERTY_ACCESS_EXPRESSION)) {
            BoundPropertyAccessExpressionNode propertyAccessNode = (BoundPropertyAccessExpressionNode) context.entry.node;
            if (propertyAccessNode.syntaxNode.callee.is(ParserNodeType.BASE_EXPRESSION)) {
                if (propertyAccessNode.syntaxNode.operator.getRange().isBefore(context.line, context.column)) {
                    return getBaseClassMethodSuggestions(parameters, context);
                }
            }
        }

        if (context.entry.node.is(BoundNodeType.INVALID_EXPRESSION)) {
            BoundInvalidExpressionNode invalidExpressionNode = (BoundInvalidExpressionNode) context.entry.node;
            boolean isBaseMethodInvocation =
                    invalidExpressionNode.unboundNodes.size() == 1 &&
                    invalidExpressionNode.unboundNodes.get(0) instanceof InvocationExpressionNode &&
                    ((InvocationExpressionNode) invalidExpressionNode.unboundNodes.get(0)).callee instanceof MemberAccessExpressionNode &&
                    ((MemberAccessExpressionNode) ((InvocationExpressionNode) invalidExpressionNode.unboundNodes.get(0)).callee).callee.is(ParserNodeType.BASE_EXPRESSION);
            if (isBaseMethodInvocation) {
                InvocationExpressionNode invocationNode = (InvocationExpressionNode) invalidExpressionNode.unboundNodes.get(0);
                MemberAccessExpressionNode memberAccessNode = (MemberAccessExpressionNode) invocationNode.callee;
                if (TextRange.isBetween(context.line, context.column, memberAccessNode.operator, invocationNode.arguments)) {
                    return getBaseClassMethodSuggestions(parameters, context);
                }
            }
        }

        if (context.entry.node.is(BoundNodeType.METHOD)) {
            if (context.entry.parent != null && context.entry.parent.node.is(BoundNodeType.BASE_METHOD_INVOCATION_EXPRESSION)) {
                return getBaseClassMethodSuggestions(parameters, context);
            }
        }

        return Lists.of();
    }

    private List<T> getBaseClassMethodSuggestions(CompilationParameters parameters, CompletionContext context) {
        BoundClassNode classNode = findClassNode(context);
        if (classNode != null) {
            List<T> suggestions = new ArrayList<>();
            SType baseType = classNode.getDeclaredType().getBaseType();
            MemberLookup.getMethods(baseType).stream()
                    .filter(m -> !m.isStatic())
                    .filter(m -> m.getVisibility() != Visibility.PRIVATE)
                    .filter(m -> {
                        if (m instanceof NativeMethodReference) {
                            NativeMethodReference nativeRef = (NativeMethodReference) m;
                            JavaInteropPolicy checker = parameters.getInteropPolicy();
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
            return Lists.of();
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