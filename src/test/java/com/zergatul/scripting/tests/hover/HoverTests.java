package com.zergatul.scripting.tests.hover;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.PropertyDescription;
import com.zergatul.scripting.documentation.DocumentationProvider;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.type.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class HoverTests extends ComparatorTest {

    @Test
    public void booleanLiteralTest() {
        assertHover("""
                let x = <cursor>true;
                """,
                4,
                List.of(
                        "boolean",
                        new DocumentationProvider().getTypeDocs(SBoolean.instance)));
    }

    @Test
    public void charLiteralTest() {
        assertHover("""
                let x = <cursor>'a';
                """,
                3,
                List.of(
                        "char",
                        new DocumentationProvider().getTypeDocs(SChar.instance)));
    }

    @Test
    public void int32LiteralTest() {
        assertHover("""
                let x = <cursor>1234;
                """,
                4,
                List.of(
                        "int",
                        new DocumentationProvider().getTypeDocs(SInt.instance)));
    }

    @Test
    public void int64LiteralTest() {
        assertHover("""
                let x = <cursor>123L;
                """,
                4,
                List.of(
                        "long",
                        new DocumentationProvider().getTypeDocs(SInt64.instance)));
    }

    @Test
    public void floatLiteralTest() {
        assertHover("""
                let x = <cursor>0.03;
                """,
                4,
                List.of(
                        "float",
                        new DocumentationProvider().getTypeDocs(SFloat.instance)));
    }

    @Test
    public void stringLiteralTest() {
        assertHover("""
                let x = <cursor>"aa";
                """,
                4,
                List.of(
                        "string",
                        new DocumentationProvider().getTypeDocs(SString.instance)));
    }

    @Test
    public void localVariableTest() {
        assertHover("""
                let <cursor>x = "aa";
                """,
                1,
                List.of("(local variable) string x"));
    }

    @Test
    public void parameterTest() {
        assertHover("""
                void func(int a) {
                    <cursor>a
                }
                """,
                1,
                List.of("(parameter) int a"));
    }

    @Test
    public void externalStaticConstantTest() {
        assertHover("""
                <cursor>intStorage
                """,
                10,
                List.of("(external static constant) com.zergatul.scripting.tests.compiler.helpers.IntStorage intStorage"));
    }

    @Test
    public void binaryOperationTest1() {
        assertHover("""
                let x = 1 <cursor>+ 2;
                """,
                1,
                List.of("int +(int left, int right)"));
    }

    @Test
    public void binaryOperationTest2() {
        assertHover("""
                let x = [1, 2, 3] <cursor>+ 4;
                """,
                1,
                List.of("int[] +(int[] left, int right)"));
    }

    @Test
    public void classTest() {
        assertHover("""
                class MyType {}
                MyType <cursor>x;
                """,
                1,
                List.of("(local variable) MyType x"));
    }

    @Test
    public void functionTest() {
        assertHover("""
                void func(int abc){}
                <cursor>func();
                """,
                4,
                List.of("(function) void func(int abc)"));
    }

    @Test
    public void methodTest() {
        assertHover("""
                "".<cursor>contains("");
                """,
                8,
                List.of("boolean string.contains(string str)"));
    }

    @Test
    public void methodDocumentationTest() {
        assertHover("""
                "".<cursor>matches("");
                """,
                7,
                List.of(
                        "boolean string.matches(string regex)",
                        "Returns true if string instance matches specified regex.\n" +
                        "For more documentation check https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html\n"));
    }

    @Test
    public void fieldPropertyDocumentationTest() {
        assertHover("""
                TestType value;
                value.<cursor>field;
                """,
                TestType.class,
                5,
                List.of(
                        "(property) int TestType.field",
                        "Field description."));
    }

    @Test
    public void getterPropertyDocumentationTest() {
        assertHover("""
                TestType value;
                value.<cursor>property;
                """,
                TestType.class,
                8,
                List.of(
                        "(property) int TestType.property",
                        "Getter description."));
    }

    @Test
    public void extensionTest1() {
        assertHover("""
                extension(int) {
                    int next() => <cursor>this + 1;
                }
                """,
                4,
                List.of("int this"));
    }

    @Test
    public void extensionTest2() {
        assertHover("""
                extension(int) {
                    int next() => this + 1;
                }
                
                (0).<cursor>next();
                """,
                4,
                List.of("(extension) int int.next()"));
    }

    @Test
    public void baseKeywordInvalidExpressionTest() {
        assertHover("""
                class ClassA {
                    void method() {}
                }
                class ClassB : ClassA {
                    void method2() {
                        <cursor>base.
                    }
                }
                """,
                4,
                List.of("ClassA base"));
    }

    @Test
    public void baseKeywordValidExpressionTest() {
        assertHover("""
                class ClassA {
                    void method() {}
                }
                class ClassB : ClassA {
                    void method2() {
                        <cursor>base.method();
                    }
                }
                """,
                4,
                List.of("ClassA base"));
    }

    @Test
    public void classInitializerTest1() {
        assertHover("""
                class ClassA {
                    constructor(int x) {}
                }
                class ClassB : ClassA {
                    constructor(int a, int b) : <cursor>base(a + b) {}
                }
                """,
                4,
                List.of("constructor ClassA(int x)"));
    }

    @Test
    public void classInitializerTest2() {
        assertHover("""
                class ClassA {
                    constructor(int x) {}
                    constructor(int x, int y) : <cursor>this(x + y) {}
                }
                """,
                4,
                List.of("constructor ClassA(int x)"));
    }

    @Test
    public void typeAliasTest1() {
        assertHover("""
                typealias Int = int;
                <cursor>Int i = 123;
                """,
                3,
                List.of("typealias Int = int"));
    }

    @Test
    public void typeAliasTest2() {
        assertHover("""
                typealias Int1 = Int2;
                typealias Int2 = Int3;
                typealias Int3 = Int4;
                typealias Int4 = int;
                <cursor>Int1 i = 123;
                """,
                4,
                List.of("typealias Int1 = int"));
    }

    @Test
    public void patternVariableTest() {
        assertHover("""
                typealias ArrayList = Java<java.util.ArrayList>;
                if (new ArrayList().get(0) is string <cursor>str) {}
                """,
                3,
                List.of("(local variable) string str"));
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