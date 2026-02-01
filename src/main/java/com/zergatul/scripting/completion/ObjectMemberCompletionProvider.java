package com.zergatul.scripting.completion;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.JavaInteropPolicy;
import com.zergatul.scripting.parser.nodes.MemberAccessExpressionNode;
import com.zergatul.scripting.parser.nodes.ParserNodeType;
import com.zergatul.scripting.type.NativeMethodReference;
import com.zergatul.scripting.type.PropertyReference;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.SUnknown;

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
                if (TextRange.combineFromEnd(propertyAccess.syntaxNode.operator, propertyAccess.property).containsOrEnds(context.line, context.column)) {
                    return getMembers(output, parameters, propertyAccess.callee.type, propertyAccess.syntaxNode.isPrivate());
                }
            }
            case METHOD_INVOCATION_EXPRESSION -> {
                BoundMethodInvocationExpressionNode methodInvocation = (BoundMethodInvocationExpressionNode) context.entry.node;
                if (TextRange.combineFromEnd(methodInvocation.getDotToken(), methodInvocation.method).containsOrEnds(context.line, context.column)) {
                    if (methodInvocation.syntaxNode.callee.is(ParserNodeType.MEMBER_ACCESS_EXPRESSION)) {
                        MemberAccessExpressionNode memberAccess = (MemberAccessExpressionNode) methodInvocation.syntaxNode.callee;
                        return getMembers(output, parameters, methodInvocation.objectReference.type, memberAccess.isPrivate());
                    } else {
                        return getMembers(output, parameters, methodInvocation.objectReference.type, false);
                    }
                }
            }
            case PROPERTY, METHOD -> {
                return provide(parameters, output, context.up());
            }
        }

        return List.of();
    }

    private List<T> getMembers(BinderOutput output, CompilationParameters parameters, SType type, boolean isPrivate) {
        if (type == SUnknown.instance) {
            return List.of();
        }

        List<T> suggestions = new ArrayList<>();

        type.getInstanceProperties().stream()
                .filter(p -> p.isPublic() ^ isPrivate)
                .forEach(p -> suggestions.add(factory.getPropertySuggestion(p)));

        type.getInstanceMethods().stream()
                .filter(p -> p.isPublic() ^ isPrivate)
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

        for (BoundCompilationUnitMemberNode memberNode : output.unit().members.members) {
            if (memberNode.isNot(BoundNodeType.EXTENSION_DECLARATION)) {
                continue;
            }

            BoundExtensionNode extensionNode = (BoundExtensionNode) memberNode;
            if (!extensionNode.typeNode.type.equals(type)) {
                continue;
            }

            for (BoundExtensionMemberNode extMemberNode : extensionNode.members) {
                if (extMemberNode instanceof BoundExtensionMethodNode methodNode) {
                    suggestions.add(factory.getMethodSuggestion(methodNode.method));
                }
            }
        }

        return suggestions;
    }
}