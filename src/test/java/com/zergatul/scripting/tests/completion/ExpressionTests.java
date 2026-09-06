package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.*;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.expressions;
import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.types;

public class ExpressionTests {

    @Test
    public void commentAfterDotTest() {
        String code =
                "let x = 123;\n" +
                "x.<cursor>//\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SInt.instance, "toInt8"),
                        MethodSuggestion.getInstance(SInt.instance, "toInt16"),
                        MethodSuggestion.getInstance(SInt.instance, "toString"),
                        MethodSuggestion.getInstance(SInt.instance, "toStandardString")));
    }

    @Test
    public void newExpressionTest1() {
        String code =
                "let x = new <cursor>\n";
        assertSuggestions(
                code,
                context -> types);
    }

    @Test
    public void newExpressionTest2() {
        String code =
                "let x = new a<cursor>\n";
        assertSuggestions(
                code,
                context -> types);
    }

    @Test
    public void binaryExpressionTest1() {
        String code =
                "let x = 1 + <cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void binaryExpressionTest2() {
        String code =
                "let x = 1 + <cursor>\n" +
                "let y = 2;\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void inExpressionTest1() {
        String code =
                "let x = 1 in <cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void inExpressionTest2() {
        String code =
                "let x = 1 in <cursor>\n" +
                "let y = 2;\n";
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