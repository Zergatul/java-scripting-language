package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.AsyncRunnable;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.SJavaObject;
import com.zergatul.scripting.type.SVoidType;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.expressions;
import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.statements;

public class AwaitTests {

    @Test
    public void staticFieldTest() {
        String code =
                "static int x = <cursor>\n";
        assertAsyncSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new StaticFieldSuggestion(context, "x")));
    }

    @Test
    public void statementListTest() {
        String code =
                "int a;\n" +
                "a = <cursor>\n";
        assertAsyncSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new KeywordSuggestion(TokenType.AWAIT),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "a")));
    }

    @Test
    public void constructorTest1() {
        String code =
                "class MyClass {\n" +
                "    constructor() {<cursor>}\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ThisSuggestion(context, "MyClass"),
                        new BaseSuggestion(SJavaObject.instance),
                        new ClassSuggestion(context, "MyClass")));
    }

    @Test
    public void constructorTest2() {
        String code =
                "class MyClass {\n" +
                "    constructor() {<cursor>}\n" +
                "}\n";
        assertAsyncSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ThisSuggestion(context, "MyClass"),
                        new BaseSuggestion(SJavaObject.instance),
                        new ClassSuggestion(context, "MyClass")));
    }

    @Test
    public void methodTest1() {
        String code =
                "class MyClass {\n" +
                "    void method() {<cursor>}\n" +
                "}\n";
        assertAsyncSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ThisSuggestion(context, "MyClass"),
                        new BaseSuggestion(SJavaObject.instance),
                        new ClassSuggestion(context, "MyClass"),
                        MethodSuggestion.getInstance(context, "MyClass", "method")));
    }

    @Test
    public void methodTest2() {
        String code =
                "class MyClass {\n" +
                "    async void method() {<cursor>}\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
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
        assertAsyncSuggestions(
                "void func() {<cursor>}",
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new FunctionSuggestion(context, "func")));
    }

    @Test
    public void functionTest2() {
        assertSuggestions(
                "async void func() {<cursor>}\n",
                context -> Lists.from(
                        statements,
                        new KeywordSuggestion(TokenType.AWAIT),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new FunctionSuggestion(context, "func")));
    }

    @Test
    public void lambdaTest() {
        assertAsyncSuggestions(
                "fn<() => void> func = () => {<cursor>};\n",
                context -> Lists.from(
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