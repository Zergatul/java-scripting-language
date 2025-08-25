package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.Suggestion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

public class JavaTypeTests {

    @Test
    public void packageTest() {
        assertSuggestions("""
                Java<<cursor>
                """,
                context -> List.of());
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions_old(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}
