package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.KeywordSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.Suggestion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class MetaExpressionTests {

    @Test
    public void basicTest() {
        String code =
                "let x = #<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        new KeywordSuggestion(TokenType.META_CAST),
                        new KeywordSuggestion(TokenType.META_TYPE),
                        new KeywordSuggestion(TokenType.META_TYPE_OF)));
    }

    @Test
    public void metaTypeExpressionTest() {
        String code =
                "let x = #type(<cursor>)\n";
        assertSuggestions(
                code,
                context -> types);
    }

    @Test
    public void metaTypeOfExpressionTest() {
        String code =
                "let x = #typeof(<cursor>)\n";
        assertSuggestions(
                code,
                context -> expressions);
    }

    @Test
    public void metaCastExpressionTest1() {
        String code =
                "let x = #cast(<cursor>)\n";
        assertSuggestions(
                code,
                context -> expressions);
    }

    @Test
    public void metaCastExpressionTest2() {
        String code =
                "let x = #cast(1,<cursor>)\n";
        assertSuggestions(
                code,
                context -> types);
    }

    @Test
    public void metaCastExpressionTest3() {
        String code =
                "let x = #cast(<cursor>\n";
        assertSuggestions(
                code,
                context -> expressions);
    }

    @Test
    public void metaCastExpressionTest4() {
        String code =
                "let x = #cast(1,<cursor>\n";
        assertSuggestions(
                code,
                context -> types);
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {}
}