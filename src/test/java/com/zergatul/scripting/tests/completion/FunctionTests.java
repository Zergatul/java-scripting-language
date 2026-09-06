package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class FunctionTests {

    @Test
    public void parameterTest1() {
        String code =
                "void f(<cursor>) {}\n";
        assertSuggestions(
                code,
                context -> types);
    }

    @Test
    public void parameterTest2() {
        String code =
                "void f(i<cursor>) {}\n";
        assertSuggestions(
                code,
                context -> types);
    }

    @Test
    public void parameterTest3() {
        String code =
                "void f(int<cursor> ) {}\n";
        assertSuggestions(
                code,
                context -> types);
    }

    @Test
    public void parameterTest4() {
        String code =
                "void f(int <cursor>) {}\n";
        assertSuggestions(
                code,
                context -> Lists.of());
    }

    @Test
    public void parameterTest5() {
        String code =
                "void f(int x<cursor>) {}\n";
        assertSuggestions(
                code,
                context -> Lists.of());
    }

    @Test
    public void parameterTest6() {
        String code =
                "void f(int x,<cursor>) {}\n";
        assertSuggestions(
                code,
                context -> types);
    }

    @Test
    public void simpleTest() {
        String code =
                "void func() {}\n" +
                "int x = 3;\n" +
                "<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new FunctionSuggestion(context, "func"),
                        new LocalVariableSuggestion(context, "x")));
    }

    @Test
    public void asyncFunctionTest1() {
        String code =
                "async <cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        types,
                        new KeywordSuggestion(TokenType.VOID)));
    }

    @Test
    public void asyncFunctionTest2() {
        String code =
                "async void <cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.of());
    }

    @Test
    public void functionOverloadTest() {
        String code =
                "int max(int i1) => 0;\n" +
                "int max(int i1, int i2) => 0;\n" +
                "int max(int i1, int i2, int i3) => 0;\n" +
                "int max(int i1, int i2, int i3, int i4) => 0;\n" +
                "                \n" +
                "intStorage.add(1);\n" +
                "<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new FunctionSuggestion(context, "max", 1),
                        new FunctionSuggestion(context, "max", 2),
                        new FunctionSuggestion(context, "max", 3),
                        new FunctionSuggestion(context, "max", 4)));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}