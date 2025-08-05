package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.tests.completion.helpers.SuggestionHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SType;
import org.junit.jupiter.api.Assertions;

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

    public static MethodSuggestion getStatic(SType type, String name) {
        Optional<MethodReference> optional = type.getStaticMethods().stream().filter(r -> r.getName().equals(name)).findFirst();
        if (optional.isEmpty()) {
            Assertions.fail();
            throw new AssertionError();
        } else {
            return new MethodSuggestion(optional.get());
        }
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