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
    public void unfinishedNameExpressionTest() {
        assertSuggestions("""
                run.onString(str => a<cursor>);
                """,
                context -> Lists.of(
                        expressions,
                        LocalVariableSuggestion.getParameter(context, "str"),
                        new StaticConstantSuggestion(context, "run")));
    }

    @Test
    public void variableCapturingTest1() {
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

    @Test
    public void variableCapturingTest2() {
        assertSuggestions("""
                void write(string x) => {};
                
                int[] players = [];
                run.onString(str1 => {
                    players += 10;
                    write("A: " + p<cursor>)
                });
                """,
                context -> Lists.of(
                        expressions,
                        LocalVariableSuggestion.getParameter(context, "str1"),
                        new LocalVariableSuggestion(context, "players"),
                        new FunctionSuggestion(context, "write"),
                        new StaticConstantSuggestion(context, "run")));
    }

    @Test
    public void singleWordStatementStartTest() {
        assertSuggestions("""
                int x = 1;
                fn<int => int> mapper = value => f<cursor>
                x = 2;
                """,
                context -> Lists.of(
                        expressions,
                        new StaticConstantSuggestion(context, "run"),
                        new LocalVariableSuggestion(context, "x"),
                        new LocalVariableSuggestion(context, "mapper"),
                        LocalVariableSuggestion.getParameter(context, "value")));
    }

    @Test
    public void lambdaStartBetweenStatementsTest() {
        // in this case "value" is SUnknown, but that's ok, because as soon as we have at least 1 character
        // "value" will get bound properly
        assertSuggestions("""
                int x = 1;
                fn<int => int> mapper = value => <cursor>
                x = 2;
                """,
                context -> Lists.of(
                        expressions,
                        new StaticConstantSuggestion(context, "run"),
                        new LocalVariableSuggestion(context, "x"),
                        LocalVariableSuggestion.getParameter(context, "value")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static Run run;
    }
}