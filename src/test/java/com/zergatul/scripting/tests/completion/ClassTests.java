package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.SAliasType;
import com.zergatul.scripting.type.SJavaObject;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class ClassTests {

    @Test
    public void suggestClassAsTypeTest() {
        String code =
                "class Class {}\n" +
                "<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        unitMembers,
                        statements,
                        new KeywordSuggestion(TokenType.ASYNC),
                        new ClassSuggestion(context, "Class"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void memberSuggestionsTest1() {
        String code =
                "class Class {\n" +
                "    <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        types,
                        new KeywordSuggestion(TokenType.VOID),
                        new KeywordSuggestion(TokenType.ASYNC),
                        new KeywordSuggestion(TokenType.VIRTUAL),
                        new KeywordSuggestion(TokenType.OVERRIDE),
                        new KeywordSuggestion(TokenType.PUBLIC),
                        new KeywordSuggestion(TokenType.PROTECTED),
                        new KeywordSuggestion(TokenType.PRIVATE),
                        new ClassSuggestion(context, "Class"),
                        new KeywordSuggestion(TokenType.CONSTRUCTOR)));
    }

    @Test
    public void memberSuggestionsTest2() {
        String code =
                "class Class {\n" +
                "    int x;\n" +
                "    <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        types,
                        new KeywordSuggestion(TokenType.VOID),
                        new KeywordSuggestion(TokenType.ASYNC),
                        new KeywordSuggestion(TokenType.VIRTUAL),
                        new KeywordSuggestion(TokenType.OVERRIDE),
                        new KeywordSuggestion(TokenType.PUBLIC),
                        new KeywordSuggestion(TokenType.PROTECTED),
                        new KeywordSuggestion(TokenType.PRIVATE),
                        new ClassSuggestion(context, "Class"),
                        new KeywordSuggestion(TokenType.CONSTRUCTOR)));
    }

    @Test
    public void memberSuggestionsTest3() {
        String code =
                "class Class {\n" +
                "    int x;\n" +
                "    <cursor>\n" +
                "    constructor(){}\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        types,
                        new KeywordSuggestion(TokenType.VOID),
                        new KeywordSuggestion(TokenType.ASYNC),
                        new KeywordSuggestion(TokenType.VIRTUAL),
                        new KeywordSuggestion(TokenType.OVERRIDE),
                        new KeywordSuggestion(TokenType.PUBLIC),
                        new KeywordSuggestion(TokenType.PROTECTED),
                        new KeywordSuggestion(TokenType.PRIVATE),
                        new ClassSuggestion(context, "Class"),
                        new KeywordSuggestion(TokenType.CONSTRUCTOR)));
    }

    @Test
    public void constructorTest() {
        String code =
                "class Class {\n" +
                "    constructor(int a, string b) {<cursor>}\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new ClassSuggestion(context, "Class"),
                        new ThisSuggestion(context, "Class"),
                        new BaseSuggestion(SJavaObject.instance),
                        new StaticConstantSuggestion(context, "intStorage"),
                        LocalVariableSuggestion.getParameter(context, "a"),
                        LocalVariableSuggestion.getParameter(context, "b")));
    }

    @Test
    public void methodTest() {
        String code =
                "class Class {\n" +
                "    void method(int x, int y) {<cursor>}\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new ClassSuggestion(context, "Class"),
                        new ThisSuggestion(context, "Class"),
                        new BaseSuggestion(SJavaObject.instance),
                        MethodSuggestion.getInstance(context, "Class", "method"),
                        new StaticConstantSuggestion(context, "intStorage"),
                        LocalVariableSuggestion.getParameter(context, "x"),
                        LocalVariableSuggestion.getParameter(context, "y")));
    }

    @Test
    public void thisTest() {
        String code =
                "class Class {\n" +
                "    int a;\n" +
                "    float b;\n" +
                "    void method1(int x, int y) {}\n" +
                "    void method2(string s) {}\n" +
                "    void method3() {\n" +
                "        this.<cursor>\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        PropertySuggestion.getInstance(context, "Class", "a"),
                        PropertySuggestion.getInstance(context, "Class", "b"),
                        MethodSuggestion.getInstance(context, "Class", "method1"),
                        MethodSuggestion.getInstance(context, "Class", "method2"),
                        MethodSuggestion.getInstance(context, "Class", "method3")));
    }

    @Test
    public void fieldSuggestionTest() {
        String code =
                "class Class {\n" +
                "    int a;\n" +
                "    float b;\n" +
                "    void method1(int x, int y) {}\n" +
                "    void method2(string s) {}\n" +
                "    void method3() {\n" +
                "        <cursor>\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ClassSuggestion(context, "Class"),
                        new ThisSuggestion(context, "Class"),
                        new BaseSuggestion(SJavaObject.instance),
                        PropertySuggestion.getInstance(context, "Class", "a"),
                        PropertySuggestion.getInstance(context, "Class", "b"),
                        MethodSuggestion.getInstance(context, "Class", "method1"),
                        MethodSuggestion.getInstance(context, "Class", "method2"),
                        MethodSuggestion.getInstance(context, "Class", "method3")));
    }

    @Test
    public void arrowMethodTest() {
        String code =
                "class Class {\n" +
                "    int a;\n" +
                "    void method(int x) => <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ClassSuggestion(context, "Class"),
                        new ThisSuggestion(context, "Class"),
                        new BaseSuggestion(SJavaObject.instance),
                        PropertySuggestion.getInstance(context, "Class", "a"),
                        MethodSuggestion.getInstance(context, "Class", "method"),
                        LocalVariableSuggestion.getParameter(context, "x")));
    }

    // TODO: constructor calls?

    @Test
    public void suggestTypesForBaseClassTest1() {
        String code =
                "class ClassA {}\n" +
                "class ClassB : <cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        new ClassSuggestion(context, "ClassA")));
    }

    @Test
    public void suggestTypesForBaseClassTest2() {
        String code =
                "class ClassA {}\n" +
                "class ClassB : <cursor>\n" +
                "let x = 123;\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        new ClassSuggestion(context, "ClassA")));
    }

    @Test
    public void suggestTypesForBaseClassTest3() {
        String code =
                "class ClassA {}\n" +
                "class ClassB : C<cursor> {}\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        new ClassSuggestion(context, "ClassA")));
    }

    @Test
    public void interfaceInheritanceTest() {
        String code =
                "typealias Runnable = Java<java.lang.Runnable>;\n" +
                "class ClassA {}\n" +
                "class ClassB : <cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        new ClassSuggestion(context, "ClassA"),
                        new TypeAliasSuggestion(new SAliasType("Runnable", SType.fromJavaType(Runnable.class)))));
    }

    @Test
    public void multipleInheritanceTest() {
        String code =
                "typealias Runnable = Java<java.lang.Runnable>;\n" +
                "class ClassA {}\n" +
                "class ClassB : ClassA, <cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        new ClassSuggestion(context, "ClassA"),
                        new TypeAliasSuggestion(new SAliasType("Runnable", SType.fromJavaType(Runnable.class)))));
    }

    @Test
    public void suggestBaseMethodsTest1() {
        String code =
                "class ClassA {\n" +
                "    void method1() {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    void method2() {\n" +
                "        <cursor>\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new ClassSuggestion(context, "ClassA"),
                        new ClassSuggestion(context, "ClassB"),
                        new ThisSuggestion(context, "ClassB"),
                        new BaseSuggestion(context, "ClassA"),
                        MethodSuggestion.getInstance(context, "ClassA", "method1"),
                        MethodSuggestion.getInstance(context, "ClassB", "method2"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void suggestBaseMethodsTest2() {
        String code =
                "class ClassA {\n" +
                "    void method1() {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    void method2() {\n" +
                "        this.<cursor>\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(context, "ClassA", "method1"),
                        MethodSuggestion.getInstance(context, "ClassB", "method2")));
    }

    @Test
    public void suggestBaseMethodsTest3() {
        String code =
                "class ClassA {\n" +
                "    void method1() {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    void method2() {\n" +
                "        base.<cursor>\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(context, "ClassA", "method1")));
    }

    @Test
    public void suggestBaseMethodsTest4() {
        String code =
                "class ClassA {\n" +
                "    void method1() {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    void method2() {\n" +
                "        base.<cursor>a();\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(context, "ClassA", "method1")));
    }

    @Test
    public void suggestBaseMethodsTest5() {
        String code =
                "class ClassA {\n" +
                "    void method1() {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    void method2() {\n" +
                "        base.a<cursor>();\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(context, "ClassA", "method1")));
    }

    @Test
    public void suggestBaseMethodsTest6() {
        String code =
                "class ClassA {\n" +
                "    void method1() {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    void method2() {\n" +
                "        base.method1<cursor>();\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(context, "ClassA", "method1")));
    }

    @Test
    public void constructorInitializerTest() {
        String code =
                "class ClassA {\n" +
                "    constructor(int v) {}\n" +
                "    constructor(int x, int y) : this(<cursor>)\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new ClassSuggestion(context, "ClassA"),
                        LocalVariableSuggestion.getParameter(context, "x"),
                        LocalVariableSuggestion.getParameter(context, "y"),
                        new BaseSuggestion(SJavaObject.instance),
                        new ThisSuggestion(context, "ClassA")));
    }

    @Test
    public void constructorArrowNameExpressionTest() {
        String code =
                "class ClassA {\n" +
                "    constructor() => t<cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new BaseSuggestion(SJavaObject.instance),
                        new ThisSuggestion(context, "ClassA"),
                        new ClassSuggestion(context, "ClassA"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void methodArrowNameExpressionTest() {
        String code =
                "class ClassA {\n" +
                "    void method() => t<cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new BaseSuggestion(SJavaObject.instance),
                        new ThisSuggestion(context, "ClassA"),
                        new ClassSuggestion(context, "ClassA"),
                        MethodSuggestion.getInstance(context, "ClassA", "method"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unaryOperatorOverloadTest1() {
        String code =
                "class ClassA {\n" +
                "    operator [+] int(ClassA instance) => <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new ClassSuggestion(context, "ClassA"),
                        LocalVariableSuggestion.getParameter(context, "instance"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unaryOperatorOverloadTest2() {
        String code =
                "class ClassA {\n" +
                "    operator [+] int(ClassA instance) {\n" +
                "        return 10 + <cursor>\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new ClassSuggestion(context, "ClassA"),
                        LocalVariableSuggestion.getParameter(context, "instance"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void binaryOperatorOverloadTest1() {
        String code =
                "class ClassA {\n" +
                "    operator [+] int(ClassA instance1, ClassA instance2) => <cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new ClassSuggestion(context, "ClassA"),
                        LocalVariableSuggestion.getParameter(context, "instance1"),
                        LocalVariableSuggestion.getParameter(context, "instance2"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void binaryOperatorOverloadTest2() {
        String code =
                "class ClassA {\n" +
                "    operator [+] int(ClassA instance1, ClassA instance2) {\n" +
                "        return 10 + <cursor>\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        expressions,
                        new ClassSuggestion(context, "ClassA"),
                        LocalVariableSuggestion.getParameter(context, "instance1"),
                        LocalVariableSuggestion.getParameter(context, "instance2"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void visibilityMembersOnThisTest() {
        String code =
                "class Class {\n" +
                "    public int publicField;\n" +
                "    protected int protectedField;\n" +
                "    private int privateField;\n" +
                "    public void publicMethod() {}\n" +
                "    protected void protectedMethod() {}\n" +
                "    private void privateMethod() {}\n" +
                "    void test() {\n" +
                "        this.<cursor>\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        PropertySuggestion.getInstance(context, "Class", "publicField"),
                        PropertySuggestion.getInstance(context, "Class", "protectedField"),
                        PropertySuggestion.getInstance(context, "Class", "privateField"),
                        MethodSuggestion.getInstance(context, "Class", "publicMethod"),
                        MethodSuggestion.getInstance(context, "Class", "protectedMethod"),
                        MethodSuggestion.getInstance(context, "Class", "privateMethod"),
                        MethodSuggestion.getInstance(context, "Class", "test")));
    }

    @Test
    public void visibilityMembersOutsideClassTest() {
        String code =
                "class Class {\n" +
                "    public int publicField;\n" +
                "    protected int protectedField;\n" +
                "    private int privateField;\n" +
                "    public void publicMethod() {}\n" +
                "    protected void protectedMethod() {}\n" +
                "    private void privateMethod() {}\n" +
                "}\n" +
                "new Class().<cursor>\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        PropertySuggestion.getInstance(context, "Class", "publicField"),
                        MethodSuggestion.getInstance(context, "Class", "publicMethod")));
    }

    @Test
    public void inheritedVisibilityMembersTest() {
        String code =
                "class Base {\n" +
                "    public int publicField;\n" +
                "    protected int protectedField;\n" +
                "    private int privateField;\n" +
                "    public void publicMethod() {}\n" +
                "    protected void protectedMethod() {}\n" +
                "    private void privateMethod() {}\n" +
                "}\n" +
                "class Child : Base {\n" +
                "    void test() {\n" +
                "        this.<cursor>\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        PropertySuggestion.getInstance(context, "Child", "publicField"),
                        PropertySuggestion.getInstance(context, "Child", "protectedField"),
                        MethodSuggestion.getInstance(context, "Child", "test"),
                        MethodSuggestion.getInstance(context, "Child", "publicMethod"),
                        MethodSuggestion.getInstance(context, "Child", "protectedMethod")));
    }

    @Test
    public void protectedMembersTest1() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.completion.ClassTests$TestClass> {\n" +
                "    constructor() {\n" +
                "        <cursor>\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.from(
                        statements,
                        new ThisSuggestion(context, "Class"),
                        new BaseSuggestion(SType.fromJavaType(TestClass.class)),
                        new ClassSuggestion(context, "Class"),
                        PropertySuggestion.getInstance(context, "Class", "field"),
                        MethodSuggestion.getInstance(context, "Class", "method"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void protectedMembersTest2() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.completion.ClassTests$TestClass> {\n" +
                "    constructor() {\n" +
                "        base.<cursor>\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        MethodSuggestion.getInstance(context, "Class", "method")));
    }

    @Test
    public void protectedMembersTest3() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.completion.ClassTests$TestClass> {\n" +
                "    constructor() {\n" +
                "        this.<cursor>\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        PropertySuggestion.getInstance(context, "Class", "field"),
                        MethodSuggestion.getInstance(context, "Class", "method")));
    }

    @Test
    public void protectedMembersOnCapturedThisInLambdaTest() {
        String code =
                "typealias Run = Java<com.zergatul.scripting.tests.compiler.helpers.Run>;\n" +
                "\n" +
                "class Class : Java<com.zergatul.scripting.tests.completion.ClassTests$TestClass> {\n" +
                "    void execute() {\n" +
                "        let run = new Run();\n" +
                "        let self = this;\n" +
                "        run.once(() => self.<cursor>);\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        PropertySuggestion.getInstance(context, "Class", "field"),
                        MethodSuggestion.getInstance(context, "Class", "method"),
                        MethodSuggestion.getInstance(context, "Class", "execute")));
    }

    @Test
    public void privateMembersOnCapturedThisInLambdaTest() {
        String code =
                "typealias Run = Java<com.zergatul.scripting.tests.compiler.helpers.Run>;\n" +
                "\n" +
                "class Class {\n" +
                "    private int field;\n" +
                "    private void method() {}\n" +
                "\n" +
                "    void execute() {\n" +
                "        let run = new Run();\n" +
                "        let self = this;\n" +
                "        run.once(() => self.<cursor>);\n" +
                "    }\n" +
                "}\n";
        assertSuggestions(
                code,
                context -> Lists.of(
                        PropertySuggestion.getInstance(context, "Class", "field"),
                        MethodSuggestion.getInstance(context, "Class", "method"),
                        MethodSuggestion.getInstance(context, "Class", "execute")));
    }

    private void assertSuggestions(String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }

    @SuppressWarnings("unused")
    public static class TestClass {

        protected int field;

        protected int method() {
            return 123;
        }
    }
}