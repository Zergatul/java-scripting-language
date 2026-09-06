package com.zergatul.scripting.tests.definition;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.analysis.Analyzer;
import com.zergatul.scripting.analysis.definition.DefinitionProvider;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Test;

public class DefinitionTests extends ComparatorTest {

    @Test
    public void variableTest() {
        String code =
                "int x = 123;\n" +
                "x.toString();\n";
        checkDefinition(
                code,
                2, 1,
                new SingleLineTextRange(1, 1, 0, 5));
    }

    @Test
    public void functionTest() {
        String code =
                "void func() {}\n" +
                "func();\n";
        checkDefinition(
                code,
                2, 1,
                new SingleLineTextRange(1, 1, 0, 11));
    }

    @Test
    public void functionParameterTest() {
        String code =
                "int func(int value) {\n" +
                "    return value + 1;\n" +
                "}\n";
        checkDefinition(
                code,
                2, 12,
                new SingleLineTextRange(1, 10, 9, 9));
    }

    @Test
    public void lambdaParameterTest() {
        String code =
                "int func(fn<int => int> mapper) {\n" +
                "    return mapper(0);\n" +
                "}\n" +
                "func(x => x + 1);\n";
        checkDefinition(
                code,
                4, 11,
                new SingleLineTextRange(4, 6, 63, 1));
    }

    @Test
    public void classTest() {
        String code =
                "class MyClass {}\n" +
                "MyClass instance = new MyClass();\n";
        checkDefinition(
                code,
                2, 1,
                new SingleLineTextRange(1, 1, 0, 13));
    }

    @Test
    public void fieldTest() {
        String code =
                "class MyClass {\n" +
                "    int value;\n" +
                "}\n" +
                "MyClass instance = new MyClass();\n" +
                "instance.value = 3;\n";
        checkDefinition(
                code,
                5, 10,
                new SingleLineTextRange(2, 5, 20, 9));
    }

    @Test
    public void constructorTest1() {
        String code =
                "class MyClass {}\n" +
                "MyClass instance = new MyClass();\n";
        checkDefinition(
                code,
                2, 26,
                new SingleLineTextRange(1, 1, 0, 13));
    }

    @Test
    public void constructorTest2() {
        String code =
                "class MyClass {\n" +
                "    constructor() {}\n" +
                "}\n" +
                "MyClass instance = new MyClass();\n";
        checkDefinition(
                code,
                4, 25,
                new SingleLineTextRange(2, 5, 20, 13));
    }

    @Test
    public void constructorTest3() {
        String code =
                "class MyClass {\n" +
                "    constructor() {}\n" +
                "    constructor(int x) : this() {}\n" +
                "}\n";
        checkDefinition(
                code,
                3, 28,
                new SingleLineTextRange(2, 5, 20, 13));
    }

    @Test
    public void constructorTest4() {
        String code =
                "class BaseClass {\n" +
                "    constructor(int x) {}\n" +
                "}\n" +
                "class ChildClass : BaseClass {\n" +
                "    constructor(int x) : base(x) {}\n" +
                "}\n";
        checkDefinition(
                code,
                5, 28,
                new SingleLineTextRange(2, 5, 22, 18));
    }

    @Test
    public void methodTest1() {
        String code =
                "class MyClass {\n" +
                "    void method(int x) {}\n" +
                "}\n" +
                "MyClass instance = new MyClass();\n" +
                "instance.method(10);\n";
        checkDefinition(
                code,
                5, 12,
                new SingleLineTextRange(2, 5, 20, 18));
    }

    @Test
    public void methodTest2() {
        String code =
                "class BaseClass {\n" +
                "    virtual void method(int x) {}\n" +
                "}\n" +
                "class ChildClass : BaseClass {\n" +
                "    override void method(int x) {\n" +
                "        base.method(x + 1);\n" +
                "    }\n" +
                "}\n";
        checkDefinition(
                code,
                6, 16,
                new SingleLineTextRange(2, 5, 22, 26));
    }

    @Test
    public void extensionMethodTest() {
        String code =
                "extension(int) {\n" +
                "    int next() => this + 1;\n" +
                "}\n" +
                "(0).next();\n";
        checkDefinition(
                code,
                4, 6,
                new SingleLineTextRange(2, 5, 21, 10));
    }

    @Test
    public void typeAliasTest1() {
        String code =
                "typealias Int1 = Int2;\n" +
                "typealias Int2 = int;\n" +
                "Int1 a;\n";
        checkDefinition(
                code,
                3, 1,
                new SingleLineTextRange(1, 1, 0, 14));
    }

    @Test
    public void typeAliasTest2() {
        String code =
                "typealias Int1 = Int2;\n" +
                "typealias Int2 = int;\n";
        checkDefinition(
                code,
                1, 19,
                new SingleLineTextRange(2, 1, 23, 14));
    }

    private void checkDefinition(String code, int line, int column, TextRange expected) {
        CompilationParameters parameters = new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .build();

        BinderOutput binderOutput = new Analyzer().analyze(code, parameters).binderOutput();
        DefinitionProvider provider = new DefinitionProvider();
        TextRange actual = provider.get(binderOutput, line, column);

        comparator.assertEquals(expected, actual);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}