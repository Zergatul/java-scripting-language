package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.compiler.helpers.Run;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class LambdaTests {

    @Test
    public void blockStatementTest() {
        String code =
                "run.onString(str => {<cursor>});\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        LocalVariableSuggestion.getParameter(context, "str"),
                        new StaticConstantSuggestion(context, "run")));
    }

    @Test
    public void expressionTest() {
        String code =
                "run.onString(str => <cursor>);\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        LocalVariableSuggestion.getParameter(context, "str"),
                        new StaticConstantSuggestion(context, "run")));
    }

    @Test
    public void unfinishedNameExpressionTest() {
        String code =
                "run.onString(str => a<cursor>);\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        LocalVariableSuggestion.getParameter(context, "str"),
                        new StaticConstantSuggestion(context, "run")));
    }

    @Test
    public void variableCapturingTest1() {
        String code =
                "let xa = 123;\n" +
                "run.onString(str1 => {\n" +
                "    let xb = 456;\n" +
                "    run.onString(str2 => {\n" +
                "        let xc = 789;\n" +
                "        run.onString(str3 => <cursor>);\n" +
                "    });\n" +
                "});\n";
        assertSuggestions(
                code,
                context -> Lists.from(
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
        String code =
                "void write(string x) => {};\n" +
                "                \n" +
                "int[] players = [];\n" +
                "run.onString(str1 => {\n" +
                "    players += 10;\n" +
                "    write(\"A: \" + p<cursor>)\n" +
                "});\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        LocalVariableSuggestion.getParameter(context, "str1"),
                        new LocalVariableSuggestion(context, "players"),
                        new FunctionSuggestion(context, "write"),
                        new StaticConstantSuggestion(context, "run")));
    }

    @Test
    public void singleWordStatementStartTest() {
        String code =
                "int x = 1;\n" +
                "fn<int => int> mapper = value => f<cursor>\n" +
                "x = 2;\n";
        assertSuggestions(
                code,
                context -> Lists.from(
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
        String code =
                "int x = 1;\n" +
                "fn<int => int> mapper = value => <cursor>\n" +
                "x = 2;\n";
        assertSuggestions(
                code,
                context -> Lists.from(
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