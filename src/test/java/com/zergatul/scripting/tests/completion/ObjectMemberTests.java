package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.MethodSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.PropertySuggestion;
import com.zergatul.scripting.tests.completion.suggestions.Suggestion;
import com.zergatul.scripting.type.SInt;
import com.zergatul.scripting.type.SType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

public class ObjectMemberTests {

    @Test
    public void intVariableNoSymbolsTest() {
        assertSuggestions("""
                int x = 123;
                x.<cursor>
                x.toString();
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(SInt.instance, "toInt16"),
                        MethodSuggestion.getInstance(SInt.instance, "toString"),
                        MethodSuggestion.getInstance(SInt.instance, "toStandardString")));
    }

    @Test
    public void intVariableWithSymbolsTest() {
        assertSuggestions("""
                int x = 123;
                x.<cursor>a
                x.toString();
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(SInt.instance, "toInt16"),
                        MethodSuggestion.getInstance(SInt.instance, "toString"),
                        MethodSuggestion.getInstance(SInt.instance, "toStandardString")));
    }

    @Test
    public void staticReferenceNoSymbolsTest() {
        assertSuggestions("""
                int x = 123;
                int.<cursor>
                x.toString();
                """,
                context -> List.of(
                        MethodSuggestion.getStatic(SInt.instance, "tryParse"),
                        PropertySuggestion.getStatic(SInt.instance, "MIN_VALUE"),
                        PropertySuggestion.getStatic(SInt.instance, "MAX_VALUE")));
    }

    @Test
    public void staticReferenceWithSymbolsTest() {
        assertSuggestions("""
                int x = 123;
                int y = 456;
                int.abc<cursor>
                x.toString();
                """,
                context -> List.of(
                        MethodSuggestion.getStatic(SInt.instance, "tryParse"),
                        PropertySuggestion.getStatic(SInt.instance, "MIN_VALUE"),
                        PropertySuggestion.getStatic(SInt.instance, "MAX_VALUE")));
    }

    @Test
    public void staticConstantSimpleTest1() {
        assertSuggestions("""
                api.<cursor>
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    @Test
    public void staticConstantSimpleTest2() {
        assertSuggestions("""
                api.abc<cursor>
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    @Test
    public void staticConstantSimpleTest3() {
        assertSuggestions("""
                api.<cursor>();
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    @Test
    public void staticConstantSimpleTest4() {
        assertSuggestions("""
                api.abc<cursor>();
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    @Test
    public void staticConstantSimpleTest5() {
        assertSuggestions("""
                let x = 1;
                api.<cursor>
                x.toString();
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    @Test
    public void staticConstantSimpleTest6() {
        assertSuggestions("""
                let x = 1;
                api.abc<cursor>
                x.toString();
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    @Test
    public void staticConstantSimpleTest7() {
        assertSuggestions("""
                let x = 1;
                api.<cursor>()
                x.toString();
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    @Test
    public void staticConstantSimpleTest8() {
        assertSuggestions("""
                let x = 1;
                api.abc<cursor>()
                x.toString();
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static SomeApi api;
    }

    public static class SomeApi {
        public void doSomething() {}
    }
}