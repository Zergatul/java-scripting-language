package com.zergatul.scripting.completion;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.JavaInteropPolicy;
import com.zergatul.scripting.parser.nodes.MemberAccessExpressionNode;
import com.zergatul.scripting.parser.nodes.ParserNodeType;
import com.zergatul.scripting.type.NativeMethodReference;
import com.zergatul.scripting.type.MemberLookup;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.PropertyReference;
import com.zergatul.scripting.type.SStaticTypeReference;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.SUnknown;
import com.zergatul.scripting.type.Visibility;

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
                    return getMembers(output, parameters, context, propertyAccess.callee, propertyAccess.syntaxNode.isPrivate());
                }
            }
            case METHOD_INVOCATION_EXPRESSION -> {
                BoundMethodInvocationExpressionNode methodInvocation = (BoundMethodInvocationExpressionNode) context.entry.node;
                if (TextRange.combineFromEnd(methodInvocation.getDotToken(), methodInvocation.method).containsOrEnds(context.line, context.column)) {
                    if (methodInvocation.syntaxNode.callee.is(ParserNodeType.MEMBER_ACCESS_EXPRESSION)) {
                        MemberAccessExpressionNode memberAccess = (MemberAccessExpressionNode) methodInvocation.syntaxNode.callee;
                        return getMembers(output, parameters, context, methodInvocation.objectReference, memberAccess.isPrivate());
                    } else {
                        return getMembers(output, parameters, context, methodInvocation.objectReference, false);
                    }
                }
            }
            case PROPERTY, METHOD -> {
                return provide(parameters, output, context.up());
            }
        }

        return List.of();
    }

    private List<T> getMembers(
            BinderOutput output,
            CompilationParameters parameters,
            CompletionContext context,
            BoundExpressionNode objectReference,
            boolean isPrivate
    ) {
        SType type = objectReference.type;
        if (type == SUnknown.instance) {
            return List.of();
        }

        List<T> suggestions = new ArrayList<>();
        boolean staticMembers = type instanceof SStaticTypeReference;
        boolean includeProtected = objectReference instanceof BoundThisExpressionNode && isInsideClass(context);

        MemberLookup.getProperties(type).stream()
                .filter(p -> p.isStatic() == staticMembers)
                .filter(p -> isVisible(p.getVisibility(), isPrivate, includeProtected))
                .forEach(p -> suggestions.add(factory.getPropertySuggestion(p)));

        MemberLookup.getMethods(type).stream()
                .filter(m -> m.isStatic() == staticMembers)
                .filter(m -> isVisible(m.getVisibility(), isPrivate, includeProtected))
                .filter(m -> {
                    if (m instanceof NativeMethodReference nativeRef) {
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

        if (isPrivate) {
            return suggestions;
        }

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

    private static boolean isVisible(Visibility visibility, boolean isPrivate, boolean includeProtected) {
        if (isPrivate) {
            return visibility != Visibility.PUBLIC;
        } else if (includeProtected) {
            return visibility != Visibility.PRIVATE;
        } else {
            return visibility == Visibility.PUBLIC;
        }
    }

    private static boolean isInsideClass(CompletionContext context) {
        for (CompletionContext current = context; current != null && current.entry != null; current = current.up()) {
            if (current.entry.node.is(BoundNodeType.CLASS_DECLARATION)) {
                return true;
            }
            if (current.entry.node.is(BoundNodeType.EXTENSION_DECLARATION)) {
                return false;
            }
        }
        return false;
    }
}