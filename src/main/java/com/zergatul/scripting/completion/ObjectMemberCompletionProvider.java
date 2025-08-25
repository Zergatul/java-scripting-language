package com.zergatul.scripting.completion;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundMethodInvocationExpressionNode;
import com.zergatul.scripting.binding.nodes.BoundPropertyAccessExpressionNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.JavaInteropPolicy;
import com.zergatul.scripting.type.NativeMethodReference;
import com.zergatul.scripting.type.SType;

import java.util.ArrayList;
import java.util.List;

public class ObjectMemberCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public ObjectMemberCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context == null || context.entry == null) {
            return List.of();
        }

        switch (context.entry.node.getNodeType()) {
            case PROPERTY_ACCESS_EXPRESSION -> {
                BoundPropertyAccessExpressionNode propertyAccess = (BoundPropertyAccessExpressionNode) context.entry.node;
                if (TextRange.combineFromEnd(propertyAccess.dot, propertyAccess.property).containsOrEnds(context.line, context.column)) {
                    return getMembers(parameters, propertyAccess.callee.type);
                }
            }
            case METHOD_INVOCATION_EXPRESSION -> {
                BoundMethodInvocationExpressionNode methodInvocation = (BoundMethodInvocationExpressionNode) context.entry.node;
                if (TextRange.combineFromEnd(methodInvocation.dot, methodInvocation.method).containsOrEnds(context.line, context.column)) {
                    return getMembers(parameters, methodInvocation.objectReference.type);
                }
            }
            case PROPERTY, METHOD -> {
                return provide(parameters, output, context.up());
            }
        }

        return List.of();
    }

    private List<T> getMembers(CompilationParameters parameters, SType type) {
        List<T> suggestions = new ArrayList<>();

        type.getInstanceProperties()
                .forEach(p -> suggestions.add(factory.getPropertySuggestion(p)));

        type.getInstanceMethods().stream()
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
    }
}