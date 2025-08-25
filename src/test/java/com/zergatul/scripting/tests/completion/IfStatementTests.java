package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.Lists;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class IfStatementTests {

    @Test
    public void conditionTest1() {
        assertSuggestions("""
                let x = 123;
                if (<cursor>
                x.toString();
                """,
                context -> Lists.of(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x")));
    }

    @Test
    public void conditionTest2() {
        assertSuggestions("""
                let x = 123;
                if (a<cursor>
                x.toString();
                """,
                context -> Lists.of(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x")));
    }

    @Test
    public void conditionTest3() {
        assertSuggestions("""
                let x = 123;
                if (<cursor>)
                x.toString();
                """,
                context -> Lists.of(
                        expressions,
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
                """,
                context -> Lists.of(
                        expressions,
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
                """,
                context -> Lists.of(
                        statements,
                        new KeywordSuggestion(TokenType.ELSE),
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
                """,
                context -> Lists.of(
                        statements,
                        new KeywordSuggestion(TokenType.ELSE),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x"),
                        new LocalVariableSuggestion(context, "y")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions_old(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}