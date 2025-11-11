package com.zergatul.scripting.tests.hover;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.hover.DocumentationProvider;
import com.zergatul.scripting.hover.HoverProvider;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.type.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class HoverTests extends ComparatorTest {

    @Test
    public void booleanLiteralTest() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = true;
                """, 1, 9);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of(
                            "boolean",
                            new DocumentationProvider().getTypeDocs(SBoolean.instance)),
                        new SingleLineTextRange(1, 9, 8, 4)),
                hover);
    }

    @Test
    public void charLiteralTest() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = 'a';
                """, 1, 9);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of(
                                "char",
                                new DocumentationProvider().getTypeDocs(SChar.instance)),
                        new SingleLineTextRange(1, 9, 8, 3)),
                hover);
    }

    @Test
    public void int32LiteralTest() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = 1234;
                """, 1, 9);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of(
                                "int",
                                new DocumentationProvider().getTypeDocs(SInt.instance)),
                        new SingleLineTextRange(1, 9, 8, 4)),
                hover);
    }

    @Test
    public void int64LiteralTest() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = 123L;
                """, 1, 9);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of(
                                "long",
                                new DocumentationProvider().getTypeDocs(SInt64.instance)),
                        new SingleLineTextRange(1, 9, 8, 4)),
                hover);
    }

    @Test
    public void floatLiteralTest() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = 0.03;
                """, 1, 9);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of(
                                "float",
                                new DocumentationProvider().getTypeDocs(SFloat.instance)),
                        new SingleLineTextRange(1, 9, 8, 4)),
                hover);
    }

    @Test
    public void stringLiteralTest() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = "aa";
                """, 1, 9);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of(
                                "string",
                                new DocumentationProvider().getTypeDocs(SString.instance)),
                        new SingleLineTextRange(1, 9, 8, 4)),
                hover);
    }

    @Test
    public void localVariableTest() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = "aa";
                """, 1, 5);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("(local variable) string x"),
                        new SingleLineTextRange(1, 5, 4, 1)),
                hover);
    }

    @Test
    public void parameterTest() {
        HoverProvider.HoverResponse hover = getHover("""
                void func(int a) {
                    a
                }
                """, 2, 5);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("(parameter) int a"),
                        new SingleLineTextRange(2, 5, 23, 1)),
                hover);
    }

    @Test
    public void externalStaticConstantTest() {
        HoverProvider.HoverResponse hover = getHover("""
                intStorage
                """, 1, 1);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("(external static constant) com.zergatul.scripting.tests.compiler.helpers.IntStorage intStorage"),
                        new SingleLineTextRange(1, 1, 0, 10)),
                hover);
    }

    @Test
    public void binaryOperationTest1() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = 1 + 2;
                """, 1, 11);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("int +(int left, int right)"),
                        new SingleLineTextRange(1, 11, 10, 1)),
                hover);
    }

    @Test
    public void binaryOperationTest2() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = [1, 2, 3] + 4;
                """, 1, 19);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("int[] +(int[] left, int right)"),
                        new SingleLineTextRange(1, 19, 18, 1)),
                hover);
    }

    @Test
    public void classTest() {
        HoverProvider.HoverResponse hover = getHover("""
                class MyType {}
                MyType x;
                """, 2, 8);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("(local variable) MyType x"),
                        new SingleLineTextRange(2, 8, 23, 1)),
                hover);
    }

    @Test
    public void functionTest() {
        HoverProvider.HoverResponse hover = getHover("""
                void func(int abc){}
                func();
                """, 2, 1);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("(function) void func(int abc)"),
                        new SingleLineTextRange(2, 1, 21, 4)),
                hover);
    }

    @Test
    public void methodTest() {
        HoverProvider.HoverResponse hover = getHover("""
                "".contains("");
                """, 1, 4);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("boolean string.contains(string str)"),
                        new SingleLineTextRange(1, 4, 3, 8)),
                hover);
    }

    @Test
    public void extensionTest1() {
        HoverProvider.HoverResponse hover = getHover("""
                extension(int) {
                    int next() => this + 1;
                }
                """, 2, 20);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("int this"),
                        new SingleLineTextRange(2, 19, 35, 4)),
                hover);
    }

    @Test
    public void extensionTest2() {
        HoverProvider.HoverResponse hover = getHover("""
                extension(int) {
                    int next() => this + 1;
                }
                
                (0).next();
                """, 5, 6);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("(extension) int int.next()"),
                        new SingleLineTextRange(5, 5, 52, 4)),
                hover);
    }

    @Test
    public void baseKeywordInvalidExpressionTest() {
        HoverProvider.HoverResponse hover = getHover("""
                class ClassA {
                    void method() {}
                }
                class ClassB : ClassA {
                    void method2() {
                        base.
                    }
                }
                """, 6, 9);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("ClassA base"),
                        new SingleLineTextRange(6, 9, 91, 4)),
                hover);
    }

    @Test
    public void baseKeywordValidExpressionTest() {
        HoverProvider.HoverResponse hover = getHover("""
                class ClassA {
                    void method() {}
                }
                class ClassB : ClassA {
                    void method2() {
                        base.method();
                    }
                }
                """, 6, 9);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("ClassA base"),
                        new SingleLineTextRange(6, 9, 91, 4)),
                hover);
    }

    @Test
    public void classInitializerTest1() {
        HoverProvider.HoverResponse hover = getHover("""
                class ClassA {
                    constructor(int x) {}
                }
                class ClassB : ClassA {
                    constructor(int a, int b) : base(a + b) {}
                }
                """, 5, 34);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("constructor ClassA(int x)"),
                        new SingleLineTextRange(5, 33, 99, 4)),
                hover);
    }

    @Test
    public void classInitializerTest2() {
        HoverProvider.HoverResponse hover = getHover("""
                class ClassA {
                    constructor(int x) {}
                    constructor(int x, int y) : this(x + y) {}
                }
                """, 3, 34);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("constructor ClassA(int x)"),
                        new SingleLineTextRange(3, 33, 73, 4)),
                hover);
    }

    @Test
    public void typeAliasTest1() {
        HoverProvider.HoverResponse hover = getHover("""
                typealias Int = int;
                Int i = 123;
                """, 2, 1);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("typealias Int = int"),
                        new SingleLineTextRange(2, 1, 21, 3)),
                hover);
    }

    @Test
    public void typeAliasTest2() {
        HoverProvider.HoverResponse hover = getHover("""
                typealias Int1 = Int2;
                typealias Int2 = Int3;
                typealias Int3 = Int4;
                typealias Int4 = int;
                Int1 i = 123;
                """, 5, 1);
        comparator.assertEquals(
                new HoverProvider.HoverResponse(
                        List.of("typealias Int1 = int"),
                        new SingleLineTextRange(5, 1, 91, 4)),
                hover);
    }

    private static HoverProvider.HoverResponse getHover(String code, int line, int column) {
        return getHover(code, ApiRoot.class, Runnable.class, line, column);
    }

    private static HoverProvider.HoverResponse getHover(String code, Class<?> root, Class<?> functionalInterface, int line, int column) {
        CompilationParameters parameters = new CompilationParametersBuilder()
                .setRoot(root)
                .setInterface(functionalInterface)
                .build();

        LexerInput lexerInput = new LexerInput(code);
        Lexer lexer = new Lexer(lexerInput);
        LexerOutput lexerOutput = lexer.lex();

        Parser parser = new Parser(lexerOutput);
        ParserOutput parserOutput = parser.parse();

        Binder binder = new Binder(parserOutput, parameters);
        BinderOutput binderOutput = binder.bind();

        HoverProvider provider = new HoverProvider();
        return provider.get(binderOutput, line, column);
    }

    private static class ApiRoot {
        public static IntStorage intStorage;
    }
}