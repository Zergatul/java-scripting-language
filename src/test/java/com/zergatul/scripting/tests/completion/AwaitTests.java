package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.AsyncRunnable;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.Lists;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.SJavaObject;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.SVoidType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.expressions;
import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.statements;

public class AwaitTests {

    @Test
    public void staticFieldTest() {
        assertAsyncSuggestions("""
                static int x = <cursor>
                """,
                context -> Lists.of(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new StaticFieldSuggestion(context, "x")));
    }

    @Test
    public void statementListTest() {
        assertAsyncSuggestions("""
                int a;
                a = <cursor>
                """,
                context -> Lists.of(
                        expressions,
                        new KeywordSuggestion(TokenType.AWAIT),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "a")));
    }

    @Test
    public void constructorTest1() {
        assertSuggestions("""
                class MyClass {
                    constructor() {<cursor>}
                }
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ThisSuggestion(context, "MyClass"),
                        new BaseSuggestion(SJavaObject.instance),
                        new ClassSuggestion(context, "MyClass")));
    }

    @Test
    public void constructorTest2() {
        assertAsyncSuggestions("""
                class MyClass {
                    constructor() {<cursor>}
                }
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ThisSuggestion(context, "MyClass"),
                        new BaseSuggestion(SJavaObject.instance),
                        new ClassSuggestion(context, "MyClass")));
    }

    @Test
    public void methodTest1() {
        assertAsyncSuggestions("""
                class MyClass {
                    void method() {<cursor>}
                }
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ThisSuggestion(context, "MyClass"),
                        new BaseSuggestion(SJavaObject.instance),
                        new ClassSuggestion(context, "MyClass"),
                        MethodSuggestion.getInstance(context, "MyClass", "method")));
    }

    @Test
    public void methodTest2() {
        assertSuggestions("""
                class MyClass {
                    async void method() {<cursor>}
                }
                """,
                context -> Lists.of(
                        statements,
                        new KeywordSuggestion(TokenType.AWAIT),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ThisSuggestion(context, "MyClass"),
                        new BaseSuggestion(SJavaObject.instance),
                        new ClassSuggestion(context, "MyClass"),
                        MethodSuggestion.getInstance(context, "MyClass", "method")));
    }

    @Test
    public void functionTest1() {
        assertAsyncSuggestions("""
                void func() {<cursor>}
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new FunctionSuggestion(context, "func")));
    }

    @Test
    public void functionTest2() {
        assertSuggestions("""
                async void func() {<cursor>}
                """,
                context -> Lists.of(
                        statements,
                        new KeywordSuggestion(TokenType.AWAIT),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new FunctionSuggestion(context, "func")));
    }

    @Test
    public void lambdaTest() {
        assertAsyncSuggestions("""
                fn<() => void> func = () => {<cursor>};
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    private void assertAsyncSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, AsyncRunnable.class, SVoidType.instance, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}