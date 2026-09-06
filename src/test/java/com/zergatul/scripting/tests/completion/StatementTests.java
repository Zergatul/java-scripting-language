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

public class StatementTests {

    @Test
    public void unfinishedStatementBeforeObjectMember1Test() {
        String code =
                "let i = 0;\n" +
                "i<cursor>\n" +
                "intStorage.add(123);\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new LocalVariableSuggestion(context, "i"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unfinishedStatementBeforeObjectMember2Test() {
        String code =
                "let i = 0;\n" +
                "int<cursor>\n" +
                "intStorage.add(123);\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new LocalVariableSuggestion(context, "i"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unfinishedStatementBeforeObjectMember3Test() {
        String code =
                "let i = 0;\n" +
                "if<cursor>\n" +
                "intStorage.add(123);\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new LocalVariableSuggestion(context, "i"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unfinishedStatementBeforeObjectMember4Test() {
        String code =
                "let i = 0;\n" +
                "for<cursor>\n" +
                "intStorage.add(123);\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new LocalVariableSuggestion(context, "i"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unfinishedStatementBeforeObjectMember5Test() {
        String code =
                "let i = 0;\n" +
                "foreach<cursor>\n" +
                "intStorage.add(123);\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new LocalVariableSuggestion(context, "i"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unfinishedStatementBeforeObjectMember6Test() {
        String code =
                "let i = 0;\n" +
                "while<cursor>\n" +
                "intStorage.add(123);\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new LocalVariableSuggestion(context, "i"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unfinishedStatementBeforeObjectMember7Test() {
        String code =
                "let i = 0;\n" +
                "return<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                    statements,
                    new LocalVariableSuggestion(context, "i"),
                    new StaticConstantSuggestion(context, "intStorage")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}