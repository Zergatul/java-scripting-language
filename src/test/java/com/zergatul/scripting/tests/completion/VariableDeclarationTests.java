package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.LocalVariableSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.Suggestion;
import com.zergatul.scripting.tests.completion.suggestions.TypeSuggestion;
import com.zergatul.scripting.type.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

public class VariableDeclarationTests {

    @Test
    public void unfinishedInitExpressionTest() {
        assertSuggestions("""
                int x = a<cursor>
                """, context -> List.of(
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance),
                new LocalVariableSuggestion(context, "x"))); // ?
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {}
}
