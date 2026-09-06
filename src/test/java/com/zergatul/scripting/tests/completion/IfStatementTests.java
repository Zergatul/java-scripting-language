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

public class IfStatementTests {

    @Test
    public void conditionTest1() {
        String code =
                "let x = 123;\n" +
                "if (<cursor>\n" +
                "x.toString();\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x")));
    }

    @Test
    public void conditionTest2() {
        String code =
                "let x = 123;\n" +
                "if (a<cursor>\n" +
                "x.toString();\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x")));
    }

    @Test
    public void conditionTest3() {
        String code =
                "let x = 123;\n" +
                "if (<cursor>)\n" +
                "x.toString();\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x")));
    }

    @Test
    public void conditionTest4() {
        String code =
                "let x = 123;\n" +
                "let y = 456;\n" +
                "if ( <cursor> )\n" +
                "x.toString();\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x"),
                        new LocalVariableSuggestion(context, "y")));
    }

    @Test
    public void thenTest1() {
        String code =
                "if (true) <cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void elseTest1() {
        String code =
                "let x = 123;\n" +
                "let y = 456;\n" +
                "if (x > y) intStorage.add(x + y);\n" +
                "<cursor>\n" +
                "x.toString();\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new KeywordSuggestion(TokenType.ELSE),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x"),
                        new LocalVariableSuggestion(context, "y")));
    }

    @Test
    public void elseTest2() {
        String code =
                "let x = 123;\n" +
                "let y = 456;\n" +
                "if (x > y) {} <cursor>\n" +
                "x.toString();\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new KeywordSuggestion(TokenType.ELSE),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x"),
                        new LocalVariableSuggestion(context, "y")));
    }

    @Test
    public void elseTest3() {
        String code =
                "if (false) {} else <cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void elseTest4() {
        String code =
                "if (false) intStorage.add(1); el<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new KeywordSuggestion(TokenType.ELSE),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}