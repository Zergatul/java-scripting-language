package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.tests.completion.helpers.SuggestionHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.type.MethodParameter;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SType;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class MethodSuggestion extends Suggestion {

    private final MethodReference method;

    public MethodSuggestion(MethodReference method) {
        this.method = method;
    }

    public static MethodSuggestion getInstance(TestCompletionContext context, String className, String methodName) {
        return getInstance(SuggestionHelper.extractClassType(context, className), methodName);
    }

    public static MethodSuggestion getInstance(SType type, String name) {
        Optional<MethodReference> optional = type.getInstanceMethods().stream().filter(r -> r.getName().equals(name)).findFirst();
        if (optional.isEmpty()) {
            Assertions.fail();
            throw new AssertionError();
        } else {
            return new MethodSuggestion(optional.get());
        }
    }

    public static MethodSuggestion getInstance(SType type, Method method) {
        return new MethodSuggestion(type.getInstanceMethods().stream()
                .filter(ref -> ref.getName().equals(method.getName()))
                .filter(ref -> ref.getParameters().size() == method.getParameters().length)
                .filter(ref -> ref.getReturn().equals(SType.fromJavaType(method.getReturnType())))
                .findFirst()
                .orElseThrow());
    }

    public static MethodSuggestion getStatic(SType type, String name) {
        Optional<MethodReference> optional = type.getStaticMethods().stream().filter(r -> r.getName().equals(name)).findFirst();
        if (optional.isEmpty()) {
            Assertions.fail();
            throw new AssertionError();
        } else {
            return new MethodSuggestion(optional.get());
        }
    }

    public static MethodSuggestion getExtension(TestCompletionContext context, SType type, String name) {
        for (BoundCompilationUnitMemberNode memberNode : context.output().unit().members.members) {
            if (memberNode.isNot(BoundNodeType.EXTENSION_DECLARATION)) {
                continue;
            }

            BoundExtensionNode extensionNode = (BoundExtensionNode) memberNode;
            if (!extensionNode.typeNode.type.equals(type)) {
                continue;
            }

            for (BoundExtensionMemberNode extMemberNode : extensionNode.members) {
                if (extMemberNode instanceof BoundExtensionMethodNode methodNode) {
                    if (methodNode.method.getName().equals(name)) {
                        return new MethodSuggestion(methodNode.method);
                    }
                }
            }
        }

        Assertions.fail();
        throw new AssertionError();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodSuggestion other) {
            return other.method.equals(method);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getReturn());
        builder.append(' ');
        builder.append(method.getName());
        builder.append('(');
        List<MethodParameter> parameters = method.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            builder.append(parameters.get(i).type());
            builder.append(' ');
            builder.append(parameters.get(i).name());
            if (i < parameters.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(')');
        return builder.toString();
    }
}