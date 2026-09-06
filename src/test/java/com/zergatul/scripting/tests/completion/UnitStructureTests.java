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

public class UnitStructureTests {

    @Test
    public void emptyFileTest() {
        assertSuggestions(
                "<cursor>",
                context -> Lists.from(
                        unitMembers,
                        statements,
                        new KeywordSuggestion(TokenType.ASYNC),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void beforeUnitMembersTest() {
        String code =
                "<cursor>\n" +
                "static int x = 1;\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        unitMembers,
                        types,
                        new KeywordSuggestion(TokenType.ASYNC)));
    }

    @Test
    public void afterUnitMembersTest1() {
        String code =
                "static int x = 1;\n" +
                "<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        unitMembers,
                        statements,
                        new KeywordSuggestion(TokenType.ASYNC),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new StaticFieldSuggestion(context, "x")));
    }

    @Test
    public void afterUnitMembersTest2() {
        String code =
                "static int x = 1;\n" +
                "<cursor>\n" +
                "int y = 3;\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        unitMembers,
                        statements,
                        new KeywordSuggestion(TokenType.ASYNC),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new StaticFieldSuggestion(context, "x")));
    }

    @Test
    public void afterStatementsTest() {
        String code =
                "int x = 3;\n" +
                "<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x")));
    }

    @Test
    public void staticFieldTest() {
        String code =
                "static <cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        types));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}