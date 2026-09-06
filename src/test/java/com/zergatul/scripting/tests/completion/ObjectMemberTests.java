package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.SFloat;
import com.zergatul.scripting.type.SInt;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.expressions;

public class ObjectMemberTests {

    @Test
    public void beforeDotTest() {
        String code =
                "int x = 123;\n" +
                "x<cursor>.\n" +
                "x.toString();\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "api"),
                        new LocalVariableSuggestion(context, "x")));
    }

    @Test
    public void intVariableNoSymbolsTest() {
        String code =
                "int x = 123;\n" +
                "x.<cursor>\n" +
                "x.toString();\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SInt.instance, "toInt8"),
                        MethodSuggestion.getInstance(SInt.instance, "toInt16"),
                        MethodSuggestion.getInstance(SInt.instance, "toString"),
                        MethodSuggestion.getInstance(SInt.instance, "toStandardString")));
    }

    @Test
    public void intVariableWithSymbolsTest() {
        String code =
                "int x = 123;\n" +
                "x.<cursor>a\n" +
                "x.toString();\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SInt.instance, "toInt8"),
                        MethodSuggestion.getInstance(SInt.instance, "toInt16"),
                        MethodSuggestion.getInstance(SInt.instance, "toString"),
                        MethodSuggestion.getInstance(SInt.instance, "toStandardString")));
    }

    @Test
    public void intVariableValidMethodTest() {
        String code =
                "int x = 123;\n" +
                "x.toString<cursor>();\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SInt.instance, "toInt8"),
                        MethodSuggestion.getInstance(SInt.instance, "toInt16"),
                        MethodSuggestion.getInstance(SInt.instance, "toString"),
                        MethodSuggestion.getInstance(SInt.instance, "toStandardString")));
    }

    @Test
    public void staticReferenceNoSymbolsTest() {
        String code =
                "int x = 123;\n" +
                "int.<cursor>\n" +
                "x.toString();\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getStatic(SInt.instance, "tryParse"),
                        PropertySuggestion.getStatic(SInt.instance, "MIN_VALUE"),
                        PropertySuggestion.getStatic(SInt.instance, "MAX_VALUE")));
    }

    @Test
    public void staticReferenceWithSymbolsTest() {
        String code =
                "int x = 123;\n" +
                "int y = 456;\n" +
                "int.abc<cursor>\n" +
                "x.toString();\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getStatic(SInt.instance, "tryParse"),
                        PropertySuggestion.getStatic(SInt.instance, "MIN_VALUE"),
                        PropertySuggestion.getStatic(SInt.instance, "MAX_VALUE")));
    }

    @Test
    public void staticConstantSimpleTest1() {
        String code =
                "api.<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    @Test
    public void staticConstantSimpleTest2() {
        String code =
                "api.abc<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    @Test
    public void staticConstantSimpleTest3() {
        String code =
                "api.<cursor>();\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    @Test
    public void staticConstantSimpleTest4() {
        String code =
                "api.abc<cursor>();\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    @Test
    public void staticConstantSimpleTest5() {
        String code =
                "let x = 1;\n" +
                "api.<cursor>\n" +
                "x.toString();\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    @Test
    public void staticConstantSimpleTest6() {
        String code =
                "let x = 1;\n" +
                "api.abc<cursor>\n" +
                "x.toString();\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    @Test
    public void staticConstantSimpleTest7() {
        String code =
                "let x = 1;\n" +
                "api.<cursor>()\n" +
                "x.toString();\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    @Test
    public void staticConstantSimpleTest8() {
        String code =
                "let x = 1;\n" +
                "api.abc<cursor>()\n" +
                "x.toString();\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SType.fromJavaType(SomeApi.class), "doSomething")));
    }

    @Test
    public void integerLiteralTest() {
        String code =
                "let a = 1.<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SInt.instance, "toInt8"),
                        MethodSuggestion.getInstance(SInt.instance, "toInt16"),
                        MethodSuggestion.getInstance(SInt.instance, "toString"),
                        MethodSuggestion.getInstance(SInt.instance, "toStandardString")));
    }

    @Test
    public void floatLiteralTest() {
        String code =
                "let a = 0.1.<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SFloat.instance, "toFloat32"),
                        MethodSuggestion.getInstance(SFloat.instance, "toString"),
                        MethodSuggestion.getInstance(SFloat.instance, "toStandardString")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static SomeApi api;
    }

    @SuppressWarnings("unused")
    public static class SomeApi {
        public void doSomething() {}
    }
}