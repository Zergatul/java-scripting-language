package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.LocalVariableSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.StaticConstantSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.Suggestion;
import com.zergatul.scripting.tests.completion.suggestions.TypeAliasSuggestion;
import com.zergatul.scripting.type.SAliasType;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class IsExpressionTests {

    @Test
    public void ifExpressionVariableTest1() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "                \n" +
                "Object x = \"hello\";\n" +
                "if (x is string str) {\n" +
                "    <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x"),
                        new LocalVariableSuggestion(context, "str"),
                        new TypeAliasSuggestion(new SAliasType("Object", SType.fromJavaType(Object.class)))));
    }

    @Test
    public void ifExpressionVariableTest2() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "                \n" +
                "Object x = \"hello\";\n" +
                "if (x is not string str) {\n" +
                "    <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x"),
                        new TypeAliasSuggestion(new SAliasType("Object", SType.fromJavaType(Object.class)))));
    }

    @Test
    public void ifExpressionVariableTest3() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "                \n" +
                "Object x = \"hello\";\n" +
                "if (x is string str) {\n" +
                "} else {\n" +
                "    <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x"),
                        new TypeAliasSuggestion(new SAliasType("Object", SType.fromJavaType(Object.class)))));
    }

    @Test
    public void ifExpressionVariableTest4() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "                \n" +
                "Object x = \"hello\";\n" +
                "if (x is not string str) {\n" +
                "} else {\n" +
                "    <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x"),
                        new LocalVariableSuggestion(context, "str"),
                        new TypeAliasSuggestion(new SAliasType("Object", SType.fromJavaType(Object.class)))));
    }

    @Test
    public void ifExpressionAndConditionVariableTest() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "                \n" +
                "Object o1 = \"hello\";\n" +
                "Object o2 = 1;\n" +
                "if (o1 is string str && o2 is int x) {\n" +
                "    <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "o1"),
                        new LocalVariableSuggestion(context, "o2"),
                        new LocalVariableSuggestion(context, "str"),
                        new LocalVariableSuggestion(context, "x"),
                        new TypeAliasSuggestion(new SAliasType("Object", SType.fromJavaType(Object.class)))));
    }

    @Test
    public void ifExpressionOrConditionVariableTest() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "                \n" +
                "Object o1 = \"hello\";\n" +
                "Object o2 = 1;\n" +
                "if (o1 is not string str || o2 is not int x) {\n" +
                "} else {<cursor>}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "o1"),
                        new LocalVariableSuggestion(context, "o2"),
                        new LocalVariableSuggestion(context, "str"),
                        new LocalVariableSuggestion(context, "x"),
                        new TypeAliasSuggestion(new SAliasType("Object", SType.fromJavaType(Object.class)))));
    }

    @Test
    public void ifExpressionFallthroughTest1() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "                \n" +
                "Object o1 = \"hello\";\n" +
                "Object o2 = 1;\n" +
                "if (o1 is not string str) {\n" +
                "    return;\n" +
                "}\n" +
                "<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "o1"),
                        new LocalVariableSuggestion(context, "o2"),
                        new LocalVariableSuggestion(context, "str"),
                        new TypeAliasSuggestion(new SAliasType("Object", SType.fromJavaType(Object.class)))));
    }

    @Test
    public void ifExpressionFallthroughTest2() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "                \n" +
                "Object o1 = \"hello\";\n" +
                "Object o2 = 1;\n" +
                "if (o1 is string str) {\n" +
                "    return;\n" +
                "}\n" +
                "<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "o1"),
                        new LocalVariableSuggestion(context, "o2"),
                        new TypeAliasSuggestion(new SAliasType("Object", SType.fromJavaType(Object.class)))));
    }

    @Test
    public void ifExpressionFallthroughTest3() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "                \n" +
                "Object o1 = \"hello\";\n" +
                "Object o2 = 1;\n" +
                "if (o1 is string str) {\n" +
                "    //\n" +
                "} else {\n" +
                "    return;\n" +
                "}\n" +
                "<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "o1"),
                        new LocalVariableSuggestion(context, "o2"),
                        new LocalVariableSuggestion(context, "str"),
                        new TypeAliasSuggestion(new SAliasType("Object", SType.fromJavaType(Object.class)))));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}