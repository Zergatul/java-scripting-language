package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class LoopTests {

    @Test
    public void forLoopTest1() {
        String code =
                "for (let i = 0; i < 3; i++) {\n" +
                "    <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forLoopTest2() {
        String code =
                "for (let i = 0; i < 3; i++) <cursor>\n" +
                "(12).toString();\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forLoopTest3() {
        String code =
                "for (let i = 0; i < 3; i++)<cursor> (12).toString();\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forLoopTest4() {
        String code =
                "for (let i = 0; i < 3; i++) <cursor>(12).toString();\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forEachLoopTest1() {
        String code =
                "foreach (let i in [1, 2, 3]) {\n" +
                "    <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forEachLoopTest2() {
        String code =
                "foreach (let i in [1, 2, 3])<cursor>\n" +
                "(12).toString();\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forEachLoopTest3() {
        String code =
                "foreach (let i in [1, 2, 3]) <cursor>\n" +
                "(12).toString();\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forEachLoopTest4() {
        String code =
                "foreach (let i in [1, 2, 3]) <cursor>(12).toString();\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forEachLoopTest5() {
        String code =
                "foreach (let i in [1, 2, 3]) (12).toString();\n" +
                "<cursor>\n" +
                "(34).toString();\n";
        assertSuggestions(
                code,
                context -> statements);
    }

    @Test
    public void whileLoopTest() {
        String code =
                "while (true) {\n" +
                "    <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> loopStatements);
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {}
}