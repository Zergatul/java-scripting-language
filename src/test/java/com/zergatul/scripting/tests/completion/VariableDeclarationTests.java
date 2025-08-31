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

public class VariableDeclarationTests {

    @Test
    public void unfinishedInitExpressionTest1() {
        assertSuggestions("""
                int x = a<cursor>
                """,
                context -> Lists.of(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unfinishedInitExpressionTest2() {
        assertSuggestions("""
                int a = 0;
                int b = a<cursor>
                """,
                context -> Lists.of(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "a")));
    }

    @Test
    public void variablesTest() {
        assertSuggestions("""
                int a = 0;
                int b = 1;
                a<cursor>
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "a"),
                        new LocalVariableSuggestion(context, "b")));
    }

    @Test
    public void nestedScopesTest() {
        assertSuggestions("""
                int a = 0;
                while (true) {
                    int b = 1;
                    while (true) {
                        int c = 1;
                        while (true) {
                            int d = 1;
                            <cursor>
                        }
                    }
                }
                """,
                context -> Lists.of(
                        loopStatements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "a"),
                        new LocalVariableSuggestion(context, "b"),
                        new LocalVariableSuggestion(context, "c"),
                        new LocalVariableSuggestion(context, "d")));
    }

    @Test
    public void nestedLambdaTest() {
        assertSuggestions("""
                int a = 123;
                fn<int => int> b = c => <cursor>
                """,
                context -> Lists.of(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "a"),
                        LocalVariableSuggestion.getParameter(context, "c")));
    }

    @Test
    public void variableNameTest1() {
        assertSuggestions("""
                let <cursor>
                """,
                context -> List.of());
    }

    @Test
    public void variableNameTest2() {
        assertSuggestions("""
                while (true) {
                    let <cursor>
                }
                """,
                context -> List.of());
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}
