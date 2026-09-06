package com.zergatul.scripting.tests.hover;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.PropertyDescription;
import com.zergatul.scripting.documentation.DocumentationProvider;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.type.*;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;

public class HoverTests extends ComparatorTest {

    @Test
    public void booleanLiteralTest() {
        String code =
                "let x = <cursor>true;\n";
        assertHover(
                code,
                4,
                Lists.of(
                        "boolean",
                        new DocumentationProvider().getTypeDocs(SBoolean.instance)));
    }

    @Test
    public void charLiteralTest() {
        String code =
                "let x = <cursor>'a';\n";
        assertHover(
                code,
                3,
                Lists.of(
                        "char",
                        new DocumentationProvider().getTypeDocs(SChar.instance)));
    }

    @Test
    public void int32LiteralTest() {
        String code =
                "let x = <cursor>1234;\n";
        assertHover(
                code,
                4,
                Lists.of(
                        "int",
                        new DocumentationProvider().getTypeDocs(SInt.instance)));
    }

    @Test
    public void int64LiteralTest() {
        String code =
                "let x = <cursor>123L;\n";
        assertHover(
                code,
                4,
                Lists.of(
                        "long",
                        new DocumentationProvider().getTypeDocs(SInt64.instance)));
    }

    @Test
    public void floatLiteralTest() {
        String code =
                "let x = <cursor>0.03;\n";
        assertHover(
                code,
                4,
                Lists.of(
                        "float",
                        new DocumentationProvider().getTypeDocs(SFloat.instance)));
    }

    @Test
    public void stringLiteralTest() {
        String code =
                "let x = <cursor>\"aa\";\n";
        assertHover(
                code,
                4,
                Lists.of(
                        "string",
                        new DocumentationProvider().getTypeDocs(SString.instance)));
    }

    @Test
    public void localVariableTest() {
        String code =
                "let <cursor>x = \"aa\";\n";
        assertHover(
                code,
                1,
                Lists.of("(local variable) string x"));
    }

    @Test
    public void parameterTest() {
        String code =
                "void func(int a) {\n" +
                "    <cursor>a\n" +
                "}\n";
        assertHover(
                code,
                1,
                Lists.of("(parameter) int a"));
    }

    @Test
    public void externalStaticConstantTest() {
        String code =
                "<cursor>intStorage\n";
        assertHover(
                code,
                10,
                Lists.of("(external static constant) com.zergatul.scripting.tests.compiler.helpers.IntStorage intStorage"));
    }

    @Test
    public void binaryOperationTest1() {
        String code =
                "let x = 1 <cursor>+ 2;\n";
        assertHover(
                code,
                1,
                Lists.of("int +(int left, int right)"));
    }

    @Test
    public void binaryOperationTest2() {
        String code =
                "let x = [1, 2, 3] <cursor>+ 4;\n";
        assertHover(
                code,
                1,
                Lists.of("int[] +(int[] left, int right)"));
    }

    @Test
    public void classTest() {
        String code =
                "class MyType {}\n" +
                "MyType <cursor>x;\n";
        assertHover(
                code,
                1,
                Lists.of("(local variable) MyType x"));
    }

    @Test
    public void functionTest() {
        String code =
                "void func(int abc){}\n" +
                "<cursor>func();\n";
        assertHover(
                code,
                4,
                Lists.of("(function) void func(int abc)"));
    }

    @Test
    public void methodTest() {
        String code =
                "\"\".<cursor>contains(\"\");\n";
        assertHover(
                code,
                8,
                Lists.of("boolean string.contains(string str)"));
    }

    @Test
    public void methodDocumentationTest() {
        String code =
                "\"\".<cursor>matches(\"\");\n";
        assertHover(
                code,
                7,
                Lists.of(
                        "boolean string.matches(string regex)",
                        "Returns true if string instance matches specified regex.\n" +
                        "For more documentation check https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html"));
    }

    @Test
    public void fieldPropertyDocumentationTest() {
        String code =
                "TestType value;\n" +
                "value.<cursor>field;\n";
        assertHover(
                code,
                TestType.class,
                5,
                Lists.of(
                        "(property) int TestType.field",
                        "Field description."));
    }

    @Test
    public void getterPropertyDocumentationTest() {
        String code =
                "TestType value;\n" +
                "value.<cursor>property;\n";
        assertHover(
                code,
                TestType.class,
                8,
                Lists.of(
                        "(property) int TestType.property",
                        "Getter description."));
    }

    @Test
    public void extensionTest1() {
        String code =
                "extension(int) {\n" +
                "    int next() => <cursor>this + 1;\n" +
                "}\n";
        assertHover(
                code,
                4,
                Lists.of("int this"));
    }

    @Test
    public void extensionTest2() {
        String code =
                "extension(int) {\n" +
                "    int next() => this + 1;\n" +
                "}\n" +
                "                \n" +
                "(0).<cursor>next();\n";
        assertHover(
                code,
                4,
                Lists.of("(extension) int int.next()"));
    }

    @Test
    public void baseKeywordInvalidExpressionTest() {
        String code =
                "class ClassA {\n" +
                "    void method() {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    void method2() {\n" +
                "        <cursor>base.\n" +
                "    }\n" +
                "}\n";
        assertHover(
                code,
                4,
                Lists.of("ClassA base"));
    }

    @Test
    public void baseKeywordValidExpressionTest() {
        String code =
                "class ClassA {\n" +
                "    void method() {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    void method2() {\n" +
                "        <cursor>base.method();\n" +
                "    }\n" +
                "}\n";
        assertHover(
                code,
                4,
                Lists.of("ClassA base"));
    }

    @Test
    public void classInitializerTest1() {
        String code =
                "class ClassA {\n" +
                "    constructor(int x) {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    constructor(int a, int b) : <cursor>base(a + b) {}\n" +
                "}\n";
        assertHover(
                code,
                4,
                Lists.of("constructor ClassA(int x)"));
    }

    @Test
    public void classInitializerTest2() {
        String code =
                "class ClassA {\n" +
                "    constructor(int x) {}\n" +
                "    constructor(int x, int y) : <cursor>this(x + y) {}\n" +
                "}\n";
        assertHover(
                code,
                4,
                Lists.of("constructor ClassA(int x)"));
    }

    @Test
    public void typeAliasTest1() {
        String code =
                "typealias Int = int;\n" +
                "<cursor>Int i = 123;\n";
        assertHover(
                code,
                3,
                Lists.of("typealias Int = int"));
    }

    @Test
    public void typeAliasTest2() {
        String code =
                "typealias Int1 = Int2;\n" +
                "typealias Int2 = Int3;\n" +
                "typealias Int3 = Int4;\n" +
                "typealias Int4 = int;\n" +
                "<cursor>Int1 i = 123;\n";
        assertHover(
                code,
                4,
                Lists.of("typealias Int1 = int"));
    }

    @Test
    public void patternVariableTest() {
        String code =
                "typealias ArrayList = Java<java.util.ArrayList>;\n" +
                "if (new ArrayList().get(0) is string <cursor>str) {}\n";
        assertHover(
                code,
                3,
                Lists.of("(local variable) string str"));
    }

    private static void assertHover(String code, int length, List<String> expected) {
        HoverTestHelper.assertHover(code, ApiRoot.class, length, expected);
    }

    private static void assertHover(String code, Class<?> customType, int length, List<String> expected) {
        HoverTestHelper.assertHover(code, ApiRoot.class, customType, length, expected);
    }

    @SuppressWarnings("unused")
    private static class ApiRoot {
        public static IntStorage intStorage;
    }

    @SuppressWarnings("unused")
    @CustomType(name = "TestType")
    public static class TestType {

        @PropertyDescription("Field description.")
        public int field;

        @Getter(name = "property")
        @PropertyDescription("Getter description.")
        public int getProperty() {
            return 0;
        }
    }
}