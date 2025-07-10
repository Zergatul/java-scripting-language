package com.zergatul.scripting.tests.completion;

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
        assertSuggestions("""
                let x = #<cursor>
                """,
                context -> List.of(
                        new KeywordSuggestion(TokenType.META_TYPE),
                        new KeywordSuggestion(TokenType.META_TYPE_OF)));
    }

    @Test
    public void metaTypeExpressionTest() {
        assertSuggestions("""
                let x = #type(<cursor>)
                """,
                context -> types);
    }

    @Test
    public void metaTypeOfExpressionTest() {
        assertSuggestions("""
                let x = #typeof(<cursor>)
                """,
                context -> expressions);
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {}
}