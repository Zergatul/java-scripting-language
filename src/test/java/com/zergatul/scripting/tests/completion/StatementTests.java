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

public class StatementTests {

    @Test
    public void unfinishedStatementBeforeObjectMember1Test() {
        assertSuggestions("""
                let i = 0;
                i<cursor>
                intStorage.add(123);
                """,
                context -> Lists.of(
                        statements,
                        new LocalVariableSuggestion(context, "i"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unfinishedStatementBeforeObjectMember2Test() {
        assertSuggestions("""
                let i = 0;
                int<cursor>
                intStorage.add(123);
                """,
                context -> Lists.of(
                        statements,
                        new LocalVariableSuggestion(context, "i"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unfinishedStatementBeforeObjectMember3Test() {
        assertSuggestions("""
                let i = 0;
                if<cursor>
                intStorage.add(123);
                """,
                context -> Lists.of(
                        statements,
                        new LocalVariableSuggestion(context, "i"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unfinishedStatementBeforeObjectMember4Test() {
        assertSuggestions("""
                let i = 0;
                for<cursor>
                intStorage.add(123);
                """,
                context -> Lists.of(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unfinishedStatementBeforeObjectMember5Test() {
        assertSuggestions("""
                let i = 0;
                foreach<cursor>
                intStorage.add(123);
                """,
                context -> Lists.of(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unfinishedStatementBeforeObjectMember6Test() {
        assertSuggestions("""
                let i = 0;
                while<cursor>
                intStorage.add(123);
                """,
                context -> Lists.of(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    /*@Test
    public void unfinishedStatementBeforeObjectMember7Test() {
        assertSuggestions("""
                let i = 0;
                return<cursor>
                intStorage.add(123);
                """, context -> List.of(
                new KeywordSuggestion(TokenType.LET),
                new KeywordSuggestion(TokenType.FOR),
                new KeywordSuggestion(TokenType.FOREACH),
                new KeywordSuggestion(TokenType.IF),
                new KeywordSuggestion(TokenType.WHILE),
                new KeywordSuggestion(TokenType.RETURN),
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance),
                new LocalVariableSuggestion(context, "i"),
                new StaticConstantSuggestion(context, "intStorage")));
    }*/

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}