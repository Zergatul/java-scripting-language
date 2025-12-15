package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.Lists;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.LocalVariableSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.StaticConstantSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.Suggestion;
import com.zergatul.scripting.tests.completion.suggestions.TypeAliasSuggestion;
import com.zergatul.scripting.type.SAliasType;
import com.zergatul.scripting.type.SType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class IsExpressionTests {

    @Test
    public void ifExpressionVariableTest1() {
        assertSuggestions("""
                typealias Object = Java<java.lang.Object>;
                
                Object x = "hello";
                if (x is string str) {
                    <cursor>
                }
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x"),
                        new LocalVariableSuggestion(context, "str"),
                        new TypeAliasSuggestion(new SAliasType("Object", SType.fromJavaType(Object.class)))));
    }

    @Test
    public void ifExpressionVariableTest2() {
        assertSuggestions("""
                typealias Object = Java<java.lang.Object>;
                
                Object x = "hello";
                if (x is not string str) {
                    <cursor>
                }
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x"),
                        new TypeAliasSuggestion(new SAliasType("Object", SType.fromJavaType(Object.class)))));
    }

    @Test
    public void ifExpressionVariableTest3() {
        assertSuggestions("""
                typealias Object = Java<java.lang.Object>;
                
                Object x = "hello";
                if (x is string str) {
                } else {
                    <cursor>
                }
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x"),
                        new TypeAliasSuggestion(new SAliasType("Object", SType.fromJavaType(Object.class)))));
    }

    @Test
    public void ifExpressionVariableTest4() {
        assertSuggestions("""
                typealias Object = Java<java.lang.Object>;
                
                Object x = "hello";
                if (x is not string str) {
                } else {
                    <cursor>
                }
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "x"),
                        new LocalVariableSuggestion(context, "str"),
                        new TypeAliasSuggestion(new SAliasType("Object", SType.fromJavaType(Object.class)))));
    }

    @Test
    public void ifExpressionAndConditionVariableTest() {
        assertSuggestions("""
                typealias Object = Java<java.lang.Object>;
                
                Object o1 = "hello";
                Object o2 = 1;
                if (o1 is string str && o2 is int x) {
                    <cursor>
                }
                """,
                context -> Lists.of(
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
        assertSuggestions("""
                typealias Object = Java<java.lang.Object>;
                
                Object o1 = "hello";
                Object o2 = 1;
                if (o1 is not string str || o2 is not int x) {
                } else {<cursor>}
                """,
                context -> Lists.of(
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
        assertSuggestions("""
                typealias Object = Java<java.lang.Object>;
                
                Object o1 = "hello";
                Object o2 = 1;
                if (o1 is not string str) {
                    return;
                }
                <cursor>
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "o1"),
                        new LocalVariableSuggestion(context, "o2"),
                        new LocalVariableSuggestion(context, "str"),
                        new TypeAliasSuggestion(new SAliasType("Object", SType.fromJavaType(Object.class)))));
    }

    @Test
    public void ifExpressionFallthroughTest2() {
        assertSuggestions("""
                typealias Object = Java<java.lang.Object>;
                
                Object o1 = "hello";
                Object o2 = 1;
                if (o1 is string str) {
                    return;
                }
                <cursor>
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "o1"),
                        new LocalVariableSuggestion(context, "o2"),
                        new TypeAliasSuggestion(new SAliasType("Object", SType.fromJavaType(Object.class)))));
    }

    @Test
    public void ifExpressionFallthroughTest3() {
        assertSuggestions("""
                typealias Object = Java<java.lang.Object>;
                
                Object o1 = "hello";
                Object o2 = 1;
                if (o1 is string str) {
                    //
                } else {
                    return;
                }
                <cursor>
                """,
                context -> Lists.of(
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