package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.Lists;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class ClassTests {

    @Test
    public void suggestClassAsTypeTest() {
        assertSuggestions("""
                class Class {}
                <cursor>
                """,
                context -> Lists.of(
                        unitMembers,
                        statements,
                        new KeywordSuggestion(TokenType.ASYNC),
                        new ClassSuggestion(context, "Class"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void memberSuggestionsTest1() {
        assertSuggestions("""
                class Class {
                    <cursor>
                }
                """,
                context -> Lists.of(
                        types,
                        new KeywordSuggestion(TokenType.VOID),
                        new KeywordSuggestion(TokenType.ASYNC),
                        new ClassSuggestion(context, "Class"),
                        new KeywordSuggestion(TokenType.CONSTRUCTOR)));
    }

    @Test
    public void memberSuggestionsTest2() {
        assertSuggestions("""
                class Class {
                    int x;
                    <cursor>
                }
                """,
                context -> Lists.of(
                        types,
                        new KeywordSuggestion(TokenType.VOID),
                        new KeywordSuggestion(TokenType.ASYNC),
                        new ClassSuggestion(context, "Class"),
                        new KeywordSuggestion(TokenType.CONSTRUCTOR)));
    }

    @Test
    public void memberSuggestionsTest3() {
        assertSuggestions("""
                class Class {
                    int x;
                    <cursor>
                    constructor(){}
                }
                """,
                context -> Lists.of(
                        types,
                        new KeywordSuggestion(TokenType.VOID),
                        new KeywordSuggestion(TokenType.ASYNC),
                        new ClassSuggestion(context, "Class"),
                        new KeywordSuggestion(TokenType.CONSTRUCTOR)));
    }

    @Test
    public void constructorTest() {
        assertSuggestions("""
                class Class {
                    constructor(int a, string b) {<cursor>}
                }
                """,
                context -> Lists.of(
                        statements,
                        new ClassSuggestion(context, "Class"),
                        new ThisSuggestion(context, "Class"),
                        new StaticConstantSuggestion(context, "intStorage"),
                        LocalVariableSuggestion.getParameter(context, "a"),
                        LocalVariableSuggestion.getParameter(context, "b")));
    }

    @Test
    public void methodTest() {
        assertSuggestions("""
                class Class {
                    void method(int x, int y) {<cursor>}
                }
                """,
                context -> Lists.of(
                        statements,
                        new ClassSuggestion(context, "Class"),
                        new ThisSuggestion(context, "Class"),
                        MethodSuggestion.getInstance(context, "Class", "method"),
                        new StaticConstantSuggestion(context, "intStorage"),
                        LocalVariableSuggestion.getParameter(context, "x"),
                        LocalVariableSuggestion.getParameter(context, "y")));
    }

    @Test
    public void thisTest() {
        assertSuggestions("""
                class Class {
                    int a;
                    float b;
                    void method1(int x, int y) {}
                    void method2(string s) {}
                    void method3() {
                        this.<cursor>
                    }
                }
                """,
                context -> List.of(
                        PropertySuggestion.getInstance(context, "Class", "a"),
                        PropertySuggestion.getInstance(context, "Class", "b"),
                        MethodSuggestion.getInstance(context, "Class", "method1"),
                        MethodSuggestion.getInstance(context, "Class", "method2"),
                        MethodSuggestion.getInstance(context, "Class", "method3")));
    }

    @Test
    public void fieldSuggestionTest() {
        assertSuggestions("""
                class Class {
                    int a;
                    float b;
                    void method1(int x, int y) {}
                    void method2(string s) {}
                    void method3() {
                        <cursor>
                    }
                }
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ClassSuggestion(context, "Class"),
                        new ThisSuggestion(context, "Class"),
                        PropertySuggestion.getInstance(context, "Class", "a"),
                        PropertySuggestion.getInstance(context, "Class", "b"),
                        MethodSuggestion.getInstance(context, "Class", "method1"),
                        MethodSuggestion.getInstance(context, "Class", "method2"),
                        MethodSuggestion.getInstance(context, "Class", "method3")));
    }

    @Test
    public void arrowMethodTest() {
        assertSuggestions("""
                class Class {
                    int a;
                    void method(int x) => <cursor>
                }
                """,
                context -> Lists.of(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ClassSuggestion(context, "Class"),
                        new ThisSuggestion(context, "Class"),
                        PropertySuggestion.getInstance(context, "Class", "a"),
                        MethodSuggestion.getInstance(context, "Class", "method"),
                        LocalVariableSuggestion.getParameter(context, "x")));
    }

    // TODO: constructor calls?

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}