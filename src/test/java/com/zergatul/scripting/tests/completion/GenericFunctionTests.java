package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.Lists;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.LocalVariableSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.StaticConstantSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.Suggestion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.expressions;

public class GenericFunctionTests {

    @Test
    public void parameterClosureTest() {
        assertSuggestions("""
                fn<(int) => fn<(int) => int>> func = (x) => y => x + y<cursor>;
                """,
                context -> Lists.of(
                        expressions,
                        LocalVariableSuggestion.getParameter(context, "x"),
                        LocalVariableSuggestion.getParameter(context, "y"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}