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

public class IfStatementTests {

    @Test
    public void conditionTest1() {
        assertSuggestions("""
                let x = 123;
                if (<cursor>
                x.toString();
                """, context -> List.of(
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance),
                new StaticConstantSuggestion(context, "intStorage"),
                new LocalVariableSuggestion(context, "x")));
    }

    @Test
    public void conditionTest2() {
        assertSuggestions("""
                let x = 123;
                if (a<cursor>
                x.toString();
                """, context -> List.of(
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance),
                new StaticConstantSuggestion(context, "intStorage"),
                new LocalVariableSuggestion(context, "x")));
    }

    @Test
    public void conditionTest3() {
        assertSuggestions("""
                let x = 123;
                if (<cursor>)
                x.toString();
                """, context -> List.of(
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance),
                new StaticConstantSuggestion(context, "intStorage"),
                new LocalVariableSuggestion(context, "x")));
    }

    @Test
    public void conditionTest4() {
        assertSuggestions("""
                let x = 123;
                let y = 456;
                if ( <cursor> )
                x.toString();
                """, context -> List.of(
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance),
                new StaticConstantSuggestion(context, "intStorage"),
                new LocalVariableSuggestion(context, "x"),
                new LocalVariableSuggestion(context, "y")));
    }

    @Test
    public void elseTest1() {
        assertSuggestions("""
                let x = 123;
                let y = 456;
                if (x > y) intStorage.add(x + y);
                <cursor>
                x.toString();
                """, context -> List.of(
                new KeywordSuggestion(TokenType.LET),
                new KeywordSuggestion(TokenType.FOR),
                new KeywordSuggestion(TokenType.FOREACH),
                new KeywordSuggestion(TokenType.IF),
                new KeywordSuggestion(TokenType.WHILE),
                new KeywordSuggestion(TokenType.RETURN),
                new KeywordSuggestion(TokenType.ELSE),
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance),
                new StaticConstantSuggestion(context, "intStorage"),
                new LocalVariableSuggestion(context, "x"),
                new LocalVariableSuggestion(context, "y")));
    }

    @Test
    public void elseTest2() {
        assertSuggestions("""
                let x = 123;
                let y = 456;
                if (x > y) {} <cursor>
                x.toString();
                """, context -> List.of(
                new KeywordSuggestion(TokenType.LET),
                new KeywordSuggestion(TokenType.FOR),
                new KeywordSuggestion(TokenType.FOREACH),
                new KeywordSuggestion(TokenType.IF),
                new KeywordSuggestion(TokenType.WHILE),
                new KeywordSuggestion(TokenType.RETURN),
                new KeywordSuggestion(TokenType.ELSE),
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance),
                new StaticConstantSuggestion(context, "intStorage"),
                new LocalVariableSuggestion(context, "x"),
                new LocalVariableSuggestion(context, "y")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}