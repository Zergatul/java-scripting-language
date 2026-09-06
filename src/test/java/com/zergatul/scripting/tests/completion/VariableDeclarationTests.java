package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class VariableDeclarationTests {

    @Test
    public void unfinishedInitExpressionTest1() {
        String code =
                "int x = a<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unfinishedInitExpressionTest2() {
        String code =
                "int a = 0;\n" +
                "int b = a<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "a")));
    }

    @Test
    public void variablesTest() {
        String code =
                "int a = 0;\n" +
                "int b = 1;\n" +
                "a<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "a"),
                        new LocalVariableSuggestion(context, "b")));
    }

    @Test
    public void singleWordStatementStartInvalidTypeTest() {
        String code =
                "int x = 1;\n" +
                "f<cursor>\n" +
                "x = 2;\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x")));
    }

    @Test
    public void nestedScopesTest() {
        String code =
                "int a = 0;\n" +
                "while (true) {\n" +
                "    int b = 1;\n" +
                "    while (true) {\n" +
                "        int c = 1;\n" +
                "        while (true) {\n" +
                "            int d = 1;\n" +
                "            <cursor>\n" +
                "        }\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        loopStatements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "a"),
                        new LocalVariableSuggestion(context, "b"),
                        new LocalVariableSuggestion(context, "c"),
                        new LocalVariableSuggestion(context, "d")));
    }

    @Test
    public void nestedLambdaTest() {
        String code =
                "int a = 123;\n" +
                "fn<int => int> b = c => <cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "a"),
                        LocalVariableSuggestion.getParameter(context, "c")));
    }

    @Test
    public void variableNameTest1() {
        String code =
                "let <cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.of());
    }

    @Test
    public void variableNameTest2() {
        String code =
                "while (true) {\n" +
                "    let <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.of());
    }

    @Test
    public void variableNameTest3() {
        String code =
                "while (true) {\n" +
                "    let i<cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.of());
    }

    @Test
    public void variableNameTest4() {
        String code =
                "while (true) {\n" +
                "    let a = 123;\n" +
                "    let x = a<cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new LocalVariableSuggestion(context, "a"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}