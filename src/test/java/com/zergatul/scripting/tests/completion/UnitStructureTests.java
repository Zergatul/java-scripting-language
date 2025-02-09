package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

public class UnitStructureTests {

    @Test
    public void emptyFileTest() {
        assertSuggestions("<cursor>", context -> List.of(
                new KeywordSuggestion(TokenType.STATIC),
                new KeywordSuggestion(TokenType.VOID),
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
                new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void beforeUnitMembersTest() {
        assertSuggestions("""
                <cursor>
                static int x = 1;
                """, context -> List.of(
                new KeywordSuggestion(TokenType.STATIC),
                new KeywordSuggestion(TokenType.VOID),
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance)));
    }

    @Test
    public void afterUnitMembersTest1() {
        assertSuggestions("""
                static int x = 1;
                <cursor>
                """, context -> List.of(
                new KeywordSuggestion(TokenType.STATIC),
                new KeywordSuggestion(TokenType.VOID),
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
                new StaticConstantSuggestion(context, "intStorage"),
                new StaticFieldSuggestion(context, "x")));
    }

    @Test
    public void afterUnitMembersTest2() {
        assertSuggestions("""
                static int x = 1;
                <cursor>
                int y = 3;
                """, context -> List.of(
                new KeywordSuggestion(TokenType.STATIC),
                new KeywordSuggestion(TokenType.VOID),
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
                new StaticConstantSuggestion(context, "intStorage"),
                new StaticFieldSuggestion(context, "x")));
    }

    @Test
    public void afterStatementsTest() {
        assertSuggestions("""
                int x = 3;
                <cursor>
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