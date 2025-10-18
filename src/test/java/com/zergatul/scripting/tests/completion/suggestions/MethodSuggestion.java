package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.binding.nodes.BoundCompilationUnitMemberNode;
import com.zergatul.scripting.binding.nodes.BoundExtensionMethodNode;
import com.zergatul.scripting.binding.nodes.BoundExtensionNode;
import com.zergatul.scripting.binding.nodes.BoundNodeType;
import com.zergatul.scripting.tests.completion.helpers.SuggestionHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SType;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Method;
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

            for (BoundExtensionMethodNode methodNode : extensionNode.methods) {
                if (methodNode.method.getName().equals(name)) {
                    return new MethodSuggestion(methodNode.method);
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
}