package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.SInt;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class ExtensionTests {

    @Test
    public void typeTest() {
        String code =
                "extension(<cursor>)\n";
        assertSuggestions(
                code,
                context -> types);
    }

    @Test
    public void methodBeginTest() {
        String code =
                "extension(int) {\n" +
                "    <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        types,
                        new KeywordSuggestion(TokenType.VOID),
                        new KeywordSuggestion(TokenType.ASYNC)));
    }

    @Test
    public void arrowMethodBodyTest() {
        String code =
                "extension(int) {\n" +
                "    void print(string str) => <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ThisSuggestion(SInt.instance),
                        LocalVariableSuggestion.getParameter(context, "str")));
    }

    @Test
    public void normalMethodBodyTest() {
        String code =
                "extension(int) {\n" +
                "    void print(string str) {\n" +
                "        <cursor>\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ThisSuggestion(SInt.instance),
                        LocalVariableSuggestion.getParameter(context, "str")));
    }

    @Test
    public void suggestExtensionMethodTest1() {
        String code =
                "extension(int) {\n" +
                "    int next() => this + 1;\n" +
                "    void print() {\n" +
                "        this.<cursor>\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SInt.instance, "toInt8"),
                        MethodSuggestion.getInstance(SInt.instance, "toInt16"),
                        MethodSuggestion.getInstance(SInt.instance, "toString"),
                        MethodSuggestion.getInstance(SInt.instance, "toStandardString"),
                        MethodSuggestion.getExtension(context, SInt.instance, "next"),
                        MethodSuggestion.getExtension(context, SInt.instance, "print")));
    }

    @Test
    public void suggestExtensionMethodTest2() {
        String code =
                "extension(int) {\n" +
                "    int next() => this + 1;\n" +
                "    void print() {}\n" +
                "}\n" +
                "let x = (10).<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(SInt.instance, "toInt8"),
                        MethodSuggestion.getInstance(SInt.instance, "toInt16"),
                        MethodSuggestion.getInstance(SInt.instance, "toString"),
                        MethodSuggestion.getInstance(SInt.instance, "toStandardString"),
                        MethodSuggestion.getExtension(context, SInt.instance, "next"),
                        MethodSuggestion.getExtension(context, SInt.instance, "print")));
    }

    @Test
    public void arrowNameExpressionTest() {
        String code =
                "class ClassA {\n" +
                "    void method() {}\n" +
                "}\n" +
                "extension(ClassA) {\n" +
                "    void aaa() => t<cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new ThisSuggestion(context, "ClassA"),
                        new ClassSuggestion(context, "ClassA"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unaryOperatorOverloadTest() {
        String code =
                "extension(string) {\n" +
                "    operator [+] int(string str) => <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        LocalVariableSuggestion.getParameter(context, "str"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void binaryOperatorOverloadTest() {
        String code =
                "extension(string) {\n" +
                "    operator [/] string[](string str, char separator) => <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        LocalVariableSuggestion.getParameter(context, "str"),
                        LocalVariableSuggestion.getParameter(context, "separator"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}