package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.Lists;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.types;

public class ExpressionTests {

    @Test
    public void commentAfterDotTest() {
        assertSuggestions("""
                let x = 123;
                x.<cursor>//
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(SInt.instance, "toInt8"),
                        MethodSuggestion.getInstance(SInt.instance, "toInt16"),
                        MethodSuggestion.getInstance(SInt.instance, "toString"),
                        MethodSuggestion.getInstance(SInt.instance, "toStandardString")));
    }

    @Test
    public void newExpressionTest1() {
        assertSuggestions("""
                let x = new <cursor>
                """,
                context -> types);
    }

    @Test
    public void newExpressionTest2() {
        assertSuggestions("""
                let x = new a<cursor>
                """,
                context -> types);
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}