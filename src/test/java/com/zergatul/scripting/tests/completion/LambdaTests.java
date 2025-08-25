package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.compiler.helpers.Run;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.Lists;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class LambdaTests {

    @Test
    public void blockStatementTest() {
        assertSuggestions("""
                run.onString(str => {<cursor>});
                """,
                context -> Lists.of(
                        statements,
                        LocalVariableSuggestion.getParameter(context, "str"),
                        new StaticConstantSuggestion(context, "run")));
    }

    @Test
    public void expressionTest() {
        assertSuggestions("""
                run.onString(str => <cursor>);
                """,
                context -> Lists.of(
                        expressions,
                        LocalVariableSuggestion.getParameter(context, "str"),
                        new StaticConstantSuggestion(context, "run")));
    }

    @Test
    public void variableCapturingTest() {
        assertSuggestions("""
                let xa = 123;
                run.onString(str1 => {
                    let xb = 456;
                    run.onString(str2 => {
                        let xc = 789;
                        run.onString(str3 => <cursor>);
                    });
                });
                """,
                context -> Lists.of(
                        expressions,
                        LocalVariableSuggestion.getParameter(context, "str1"),
                        LocalVariableSuggestion.getParameter(context, "str2"),
                        LocalVariableSuggestion.getParameter(context, "str3"),
                        new LocalVariableSuggestion(context, "xa"),
                        new LocalVariableSuggestion(context, "xb"),
                        new LocalVariableSuggestion(context, "xc"),
                        new StaticConstantSuggestion(context, "run")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions_old(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static Run run;
    }
}