package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.Lists;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.SInt;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class ExtensionTests {

    @Test
    public void typeTest() {
        assertSuggestions("""
                extension(<cursor>)
                """,
                context -> types);
    }

    @Test
    public void methodBeginTest() {
        assertSuggestions("""
                extension(int) {
                    <cursor>
                }
                """,
                context -> Lists.of(
                        types,
                        new KeywordSuggestion(TokenType.VOID),
                        new KeywordSuggestion(TokenType.ASYNC)));
    }

    @Test
    public void arrowMethodBodyTest() {
        assertSuggestions("""
                extension(int) {
                    void print(string str) => <cursor>
                }
                """,
                context -> Lists.of(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ThisSuggestion(SInt.instance),
                        LocalVariableSuggestion.getParameter(context, "str")));
    }

    @Test
    public void normalMethodBodyTest() {
        assertSuggestions("""
                extension(int) {
                    void print(string str) {
                        <cursor>
                    }
                }
                """,
                context -> Lists.of(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ThisSuggestion(SInt.instance),
                        LocalVariableSuggestion.getParameter(context, "str")));
    }

    @Test
    public void suggestExtensionMethodTest1() {
        assertSuggestions("""
                extension(int) {
                    int next() => this + 1;
                    void print() {
                        this.<cursor>
                    }
                }
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(SInt.instance, "toInt8"),
                        MethodSuggestion.getInstance(SInt.instance, "toInt16"),
                        MethodSuggestion.getInstance(SInt.instance, "toString"),
                        MethodSuggestion.getInstance(SInt.instance, "toStandardString"),
                        MethodSuggestion.getExtension(context, SInt.instance, "next"),
                        MethodSuggestion.getExtension(context, SInt.instance, "print")));
    }

    @Test
    public void suggestExtensionMethodTest2() {
        assertSuggestions("""
                extension(int) {
                    int next() => this + 1;
                    void print() {}
                }
                let x = (10).<cursor>
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(SInt.instance, "toInt8"),
                        MethodSuggestion.getInstance(SInt.instance, "toInt16"),
                        MethodSuggestion.getInstance(SInt.instance, "toString"),
                        MethodSuggestion.getInstance(SInt.instance, "toStandardString"),
                        MethodSuggestion.getExtension(context, SInt.instance, "next"),
                        MethodSuggestion.getExtension(context, SInt.instance, "print")));
    }

    @Test
    public void arrowNameExpressionTest() {
        assertSuggestions("""
                class ClassA {
                    void method() {}
                }
                extension(ClassA) {
                    void aaa() => t<cursor>
                }
                """,
                context -> Lists.of(
                        expressions,
                        new ThisSuggestion(context, "ClassA"),
                        new ClassSuggestion(context, "ClassA"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}
