package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.Lists;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class FunctionTests {

    @Test
    public void parameterTest1() {
        assertSuggestions("""
                void f(<cursor>) {}
                """,
                context -> types);
    }

    @Test
    public void parameterTest2() {
        assertSuggestions("""
                void f(i<cursor>) {}
                """,
                context -> types);
    }

    @Test
    public void parameterTest3() {
        assertSuggestions("""
                void f(int<cursor> ) {}
                """,
                context -> types);
    }

    @Test
    public void parameterTest4() {
        assertSuggestions("""
                void f(int <cursor>) {}
                """,
                context -> List.of());
    }

    @Test
    public void parameterTest5() {
        assertSuggestions("""
                void f(int x<cursor>) {}
                """,
                context -> List.of());
    }

    @Test
    public void parameterTest6() {
        assertSuggestions("""
                void f(int x,<cursor>) {}
                """,
                context -> types);
    }

    @Test
    public void simpleTest() {
        assertSuggestions("""
                void func() {}
                int x = 3;
                <cursor>
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new FunctionSuggestion(context, "func"),
                        new LocalVariableSuggestion(context, "x")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}