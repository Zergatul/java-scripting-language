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

public class UnitStructureTests {

    @Test
    public void emptyFileTest() {
        assertSuggestions(
                "<cursor>",
                context -> Lists.of(
                        unitMembers,
                        statements,
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void beforeUnitMembersTest() {
        assertSuggestions("""
                <cursor>
                static int x = 1;
                """,
                context -> Lists.of(
                        unitMembers,
                        types));
    }

    @Test
    public void afterUnitMembersTest1() {
        assertSuggestions("""
                static int x = 1;
                <cursor>
                """,
                context -> Lists.of(
                        unitMembers,
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new StaticFieldSuggestion(context, "x")));
    }

    @Test
    public void afterUnitMembersTest2() {
        assertSuggestions("""
                static int x = 1;
                <cursor>
                int y = 3;
                """,
                context -> Lists.of(
                        unitMembers,
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new StaticFieldSuggestion(context, "x")));
    }

    @Test
    public void afterStatementsTest() {
        assertSuggestions("""
                int x = 3;
                <cursor>
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}