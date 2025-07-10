package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.Lists;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class LoopTests {

    @Test
    public void forLoopTest1() {
        assertSuggestions("""
                for (let i = 0; i < 3; i++) {
                    <cursor>
                }
                """,
                context -> Lists.of(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forLoopTest2() {
        assertSuggestions("""
                for (let i = 0; i < 3; i++) <cursor>
                (12).toString();
                """,
                context -> Lists.of(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forLoopTest3() {
        assertSuggestions("""
                for (let i = 0; i < 3; i++)<cursor> (12).toString();
                """,
                context -> Lists.of(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forLoopTest4() {
        assertSuggestions("""
                for (let i = 0; i < 3; i++) <cursor>(12).toString();
                """,
                context -> Lists.of(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forEachLoopTest1() {
        assertSuggestions("""
                foreach (let i in [1, 2, 3]) {
                    <cursor>
                }
                """,
                context -> Lists.of(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forEachLoopTest2() {
        assertSuggestions("""
                foreach (let i in [1, 2, 3])<cursor>
                (12).toString();
                """,
                context -> Lists.of(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forEachLoopTest3() {
        assertSuggestions("""
                foreach (let i in [1, 2, 3]) <cursor>
                (12).toString();
                """,
                context -> Lists.of(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forEachLoopTest4() {
        assertSuggestions("""
                foreach (let i in [1, 2, 3]) <cursor>(12).toString();
                """,
                context -> Lists.of(
                        loopStatements,
                        new LocalVariableSuggestion(context, "i")));
    }

    @Test
    public void forEachLoopTest5() {
        assertSuggestions("""
                foreach (let i in [1, 2, 3]) (12).toString();
                <cursor>
                (34).toString();
                """,
                context -> statements);
    }

    @Test
    public void whileLoopTest() {
        assertSuggestions("""
                while (true) {
                    <cursor>
                }
                """,
                context -> loopStatements);
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {}
}