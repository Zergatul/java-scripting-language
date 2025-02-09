package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

public class LoopTests {

    @Test
    public void forLoopTest() {
        assertSuggestions("""
                for (let i = 0; i < 3; i++) {
                    <cursor>
                }
                """, context -> List.of(
                new KeywordSuggestion(TokenType.LET),
                new KeywordSuggestion(TokenType.FOR),
                new KeywordSuggestion(TokenType.FOREACH),
                new KeywordSuggestion(TokenType.IF),
                new KeywordSuggestion(TokenType.WHILE),
                new KeywordSuggestion(TokenType.RETURN),
                new KeywordSuggestion(TokenType.BREAK),
                new KeywordSuggestion(TokenType.CONTINUE),
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance),
                new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forEachLoopTest() {
        assertSuggestions("""
                foreach (let i in [1, 2, 3]) {
                    <cursor>
                }
                """, context -> List.of(
                new KeywordSuggestion(TokenType.LET),
                new KeywordSuggestion(TokenType.FOR),
                new KeywordSuggestion(TokenType.FOREACH),
                new KeywordSuggestion(TokenType.IF),
                new KeywordSuggestion(TokenType.WHILE),
                new KeywordSuggestion(TokenType.RETURN),
                new KeywordSuggestion(TokenType.BREAK),
                new KeywordSuggestion(TokenType.CONTINUE),
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance),
                new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void whileLoopTest() {
        assertSuggestions("""
                while (true) {
                    <cursor>
                }
                """, context -> List.of(
                new KeywordSuggestion(TokenType.LET),
                new KeywordSuggestion(TokenType.FOR),
                new KeywordSuggestion(TokenType.FOREACH),
                new KeywordSuggestion(TokenType.IF),
                new KeywordSuggestion(TokenType.WHILE),
                new KeywordSuggestion(TokenType.RETURN),
                new KeywordSuggestion(TokenType.BREAK),
                new KeywordSuggestion(TokenType.CONTINUE),
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance)));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {}
}