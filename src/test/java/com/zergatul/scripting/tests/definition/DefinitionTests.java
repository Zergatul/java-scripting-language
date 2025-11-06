package com.zergatul.scripting.tests.definition;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.analysis.definition.DefinitionProvider;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Test;

public class DefinitionTests extends ComparatorTest {

    @Test
    public void variableTest() {
        checkDefinition("""
                int x = 123;
                x.toString();
                """,
                2, 1,
                new SingleLineTextRange(1, 1, 0, 5));
    }

    @Test
    public void functionTest() {
        checkDefinition("""
                void func() {}
                func();
                """,
                2, 1,
                new SingleLineTextRange(1, 1, 0, 11));
    }

    @Test
    public void functionParameterTest() {
        checkDefinition("""
                int func(int value) {
                    return value + 1;
                }
                """,
                2, 12,
                new SingleLineTextRange(1, 10, 9, 9));
    }

    @Test
    public void lambdaParameterTest() {
        checkDefinition("""
                int func(fn<int => int> mapper) {
                    return mapper(0);
                }
                func(x => x + 1);
                """,
                4, 11,
                new SingleLineTextRange(4, 6, 63, 1));
    }

    @Test
    public void classTest() {
        checkDefinition("""
                class MyClass {}
                MyClass instance = new MyClass();
                """,
                2, 1,
                new SingleLineTextRange(1, 1, 0, 13));
    }

    @Test
    public void fieldTest() {
        checkDefinition("""
                class MyClass {
                    int value;
                }
                MyClass instance = new MyClass();
                instance.value = 3;
                """,
                5, 10,
                new SingleLineTextRange(2, 5, 20, 9));
    }

    @Test
    public void constructorTest1() {
        checkDefinition("""
                class MyClass {}
                MyClass instance = new MyClass();
                """,
                2, 26,
                new SingleLineTextRange(1, 1, 0, 13));
    }

    @Test
    public void constructorTest2() {
        checkDefinition("""
                class MyClass {
                    constructor() {}
                }
                MyClass instance = new MyClass();
                """,
                4, 25,
                new SingleLineTextRange(2, 5, 20, 13));
    }

    @Test
    public void constructorTest3() {
        checkDefinition("""
                class MyClass {
                    constructor() {}
                    constructor(int x) : this() {}
                }
                """,
                3, 28,
                new SingleLineTextRange(2, 5, 20, 13));
    }

    @Test
    public void constructorTest4() {
        checkDefinition("""
                class BaseClass {
                    constructor(int x) {}
                }
                class ChildClass : BaseClass {
                    constructor(int x) : base(x) {}
                }
                """,
                5, 28,
                new SingleLineTextRange(2, 5, 22, 18));
    }

    @Test
    public void methodTest1() {
        checkDefinition("""
                class MyClass {
                    void method(int x) {}
                }
                MyClass instance = new MyClass();
                instance.method(10);
                """,
                5, 12,
                new SingleLineTextRange(2, 5, 20, 18));
    }

    @Test
    public void methodTest2() {
        checkDefinition("""
                class BaseClass {
                    virtual void method(int x) {}
                }
                class ChildClass : BaseClass {
                    override void method(int x) {
                        base.method(x + 1);
                    }
                }
                """,
                6, 16,
                new SingleLineTextRange(2, 5, 22, 26));
    }

    @Test
    public void extensionMethodTest() {
        checkDefinition("""
                extension(int) {
                    int next() => this + 1;
                }
                (0).next();
                """,
                4, 6,
                new SingleLineTextRange(2, 5, 21, 10));
    }

    private void checkDefinition(String code, int line, int column, TextRange expected) {
        CompilationParameters parameters = new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .build();

        LexerInput lexerInput = new LexerInput(code);
        Lexer lexer = new Lexer(lexerInput);
        LexerOutput lexerOutput = lexer.lex();

        Parser parser = new Parser(lexerOutput);
        ParserOutput parserOutput = parser.parse();

        Binder binder = new Binder(parserOutput, parameters);
        BinderOutput binderOutput = binder.bind();

        DefinitionProvider provider = new DefinitionProvider();
        TextRange actual = provider.get(binderOutput, line, column);

        comparator.assertEquals(expected, actual);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}