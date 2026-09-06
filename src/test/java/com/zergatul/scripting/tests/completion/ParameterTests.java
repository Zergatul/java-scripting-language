package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.FunctionSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.StaticConstantSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.Suggestion;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.expressions;

public class ParameterTests {

    @Test
    public void functionParameterTest1() {
        String code =
                "void f(int a) {}\n" +
                "                \n" +
                "f(<cursor>)\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new FunctionSuggestion(context, "f")));
    }

    @Test
    public void functionParameterTest2() {
        String code =
                "void f(int a) {}\n" +
                "                \n" +
                "f(100, <cursor>)\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new FunctionSuggestion(context, "f")));
    }

    @Test
    public void methodParameterTest1() {
        String code =
                "intStorage.add(<cursor>)\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void methodParameterTest2() {
        String code =
                "intStorage.add(\"a\", <cursor>)\n";
        assertSuggestions(
                code,
                context -> Lists.from(
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