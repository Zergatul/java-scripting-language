package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.Lists;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.MethodSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.Suggestion;
import com.zergatul.scripting.type.SType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Function;

public class JavaTypeTests {

    @Test
    public void packageTest() {
        assertSuggestions("""
                Java<<cursor>
                """,
                context -> List.of());
    }

    @Test
    public void methodTest() {
        List<Suggestion> suggestions = Arrays.stream(Hashtable.class.getMethods())
                .filter(m -> m.getDeclaringClass() != Object.class)
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .map(m -> (Suggestion) MethodSuggestion.getInstance(SType.fromJavaType(Hashtable.class), m))
                .toList();
        assertSuggestions("""
                let table = new Java<java.util.Hashtable>();
                table.<cursor>
                """,
                context -> suggestions);
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}
