package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.Lists;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.FunctionSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.StaticConstantSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.Suggestion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.expressions;

public class ParameterTests {

    @Test
    public void functionParameterTest1() {
        assertSuggestions("""
                void f(int a) {}
                
                f(<cursor>)
                """,
                context -> Lists.of(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new FunctionSuggestion(context, "f")));
    }

    @Test
    public void functionParameterTest2() {
        assertSuggestions("""
                void f(int a) {}
                
                f(100, <cursor>)
                """,
                context -> Lists.of(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new FunctionSuggestion(context, "f")));
    }

    @Test
    public void methodParameterTest1() {
        assertSuggestions("""
                intStorage.add(<cursor>)
                """,
                context -> Lists.of(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void methodParameterTest2() {
        assertSuggestions("""
                intStorage.add("a", <cursor>)
                """,
                context -> Lists.of(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}