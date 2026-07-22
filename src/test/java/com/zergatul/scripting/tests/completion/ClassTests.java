package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.Lists;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.SAliasType;
import com.zergatul.scripting.type.SJavaObject;
import com.zergatul.scripting.type.SType;
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
        assertSuggestions("""
                class Class {
                    constructor(int a, string b) {<cursor>}
                }
                """,
                context -> Lists.of(
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
        assertSuggestions("""
                class Class {
                    void method(int x, int y) {<cursor>}
                }
                """,
                context -> Lists.of(
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
                        new BaseSuggestion(SJavaObject.instance),
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
                        new BaseSuggestion(SJavaObject.instance),
                        PropertySuggestion.getInstance(context, "Class", "a"),
                        MethodSuggestion.getInstance(context, "Class", "method"),
                        LocalVariableSuggestion.getParameter(context, "x")));
    }

    // TODO: constructor calls?

    @Test
    public void suggestTypesForBaseClassTest1() {
        assertSuggestions("""
                class ClassA {}
                class ClassB : <cursor>
                """,
                context -> List.of(
                        new ClassSuggestion(context, "ClassA")));
    }

    @Test
    public void suggestTypesForBaseClassTest2() {
        assertSuggestions("""
                class ClassA {}
                class ClassB : <cursor>
                let x = 123;
                """,
                context -> List.of(
                        new ClassSuggestion(context, "ClassA")));
    }

    @Test
    public void suggestTypesForBaseClassTest3() {
        assertSuggestions("""
                class ClassA {}
                class ClassB : C<cursor> {}
                """,
                context -> List.of(
                        new ClassSuggestion(context, "ClassA")));
    }

    @Test
    public void interfaceInheritanceTest() {
        assertSuggestions("""
                typealias Runnable = Java<java.lang.Runnable>;
                class ClassA {}
                class ClassB : <cursor>
                """,
                context -> List.of(
                        new ClassSuggestion(context, "ClassA"),
                        new TypeAliasSuggestion(new SAliasType("Runnable", SType.fromJavaType(Runnable.class)))));
    }

    @Test
    public void multipleInheritanceTest() {
        assertSuggestions("""
                typealias Runnable = Java<java.lang.Runnable>;
                class ClassA {}
                class ClassB : ClassA, <cursor>
                """,
                context -> List.of(
                        new ClassSuggestion(context, "ClassA"),
                        new TypeAliasSuggestion(new SAliasType("Runnable", SType.fromJavaType(Runnable.class)))));
    }

    @Test
    public void suggestBaseMethodsTest1() {
        assertSuggestions("""
                class ClassA {
                    void method1() {}
                }
                class ClassB : ClassA {
                    void method2() {
                        <cursor>
                    }
                }
                """,
                context -> Lists.of(
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
        assertSuggestions("""
                class ClassA {
                    void method1() {}
                }
                class ClassB : ClassA {
                    void method2() {
                        this.<cursor>
                    }
                }
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(context, "ClassA", "method1"),
                        MethodSuggestion.getInstance(context, "ClassB", "method2")));
    }

    @Test
    public void suggestBaseMethodsTest3() {
        assertSuggestions("""
                class ClassA {
                    void method1() {}
                }
                class ClassB : ClassA {
                    void method2() {
                        base.<cursor>
                    }
                }
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(context, "ClassA", "method1")));
    }

    @Test
    public void suggestBaseMethodsTest4() {
        assertSuggestions("""
                class ClassA {
                    void method1() {}
                }
                class ClassB : ClassA {
                    void method2() {
                        base.<cursor>a();
                    }
                }
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(context, "ClassA", "method1")));
    }

    @Test
    public void suggestBaseMethodsTest5() {
        assertSuggestions("""
                class ClassA {
                    void method1() {}
                }
                class ClassB : ClassA {
                    void method2() {
                        base.a<cursor>();
                    }
                }
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(context, "ClassA", "method1")));
    }

    @Test
    public void suggestBaseMethodsTest6() {
        assertSuggestions("""
                class ClassA {
                    void method1() {}
                }
                class ClassB : ClassA {
                    void method2() {
                        base.method1<cursor>();
                    }
                }
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(context, "ClassA", "method1")));
    }

    @Test
    public void constructorInitializerTest() {
        assertSuggestions("""
                class ClassA {
                    constructor(int v) {}
                    constructor(int x, int y) : this(<cursor>)
                }
                """,
                context -> Lists.of(
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
        assertSuggestions("""
                class ClassA {
                    constructor() => t<cursor>
                }
                """,
                context -> Lists.of(
                        expressions,
                        new BaseSuggestion(SJavaObject.instance),
                        new ThisSuggestion(context, "ClassA"),
                        new ClassSuggestion(context, "ClassA"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void methodArrowNameExpressionTest() {
        assertSuggestions("""
                class ClassA {
                    void method() => t<cursor>
                }
                """,
                context -> Lists.of(
                        expressions,
                        new BaseSuggestion(SJavaObject.instance),
                        new ThisSuggestion(context, "ClassA"),
                        new ClassSuggestion(context, "ClassA"),
                        MethodSuggestion.getInstance(context, "ClassA", "method"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unaryOperatorOverloadTest1() {
        assertSuggestions("""
                class ClassA {
                    operator [+] int(ClassA instance) => <cursor>
                }
                """,
                context -> Lists.of(
                        expressions,
                        new ClassSuggestion(context, "ClassA"),
                        LocalVariableSuggestion.getParameter(context, "instance"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void unaryOperatorOverloadTest2() {
        assertSuggestions("""
                class ClassA {
                    operator [+] int(ClassA instance) {
                        return 10 + <cursor>
                    }
                }
                """,
                context -> Lists.of(
                        expressions,
                        new ClassSuggestion(context, "ClassA"),
                        LocalVariableSuggestion.getParameter(context, "instance"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void binaryOperatorOverloadTest1() {
        assertSuggestions("""
                class ClassA {
                    operator [+] int(ClassA instance1, ClassA instance2) => <cursor>
                }
                """,
                context -> Lists.of(
                        expressions,
                        new ClassSuggestion(context, "ClassA"),
                        LocalVariableSuggestion.getParameter(context, "instance1"),
                        LocalVariableSuggestion.getParameter(context, "instance2"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void binaryOperatorOverloadTest2() {
        assertSuggestions("""
                class ClassA {
                    operator [+] int(ClassA instance1, ClassA instance2) {
                        return 10 + <cursor>
                    }
                }
                """,
                context -> Lists.of(
                        expressions,
                        new ClassSuggestion(context, "ClassA"),
                        LocalVariableSuggestion.getParameter(context, "instance1"),
                        LocalVariableSuggestion.getParameter(context, "instance2"),
                        new StaticConstantSuggestion(context, "intStorage")));
    }

    @Test
    public void visibilityMembersOnThisTest() {
        assertSuggestions("""
                class Class {
                    public int publicField;
                    protected int protectedField;
                    private int privateField;
                    public void publicMethod() {}
                    protected void protectedMethod() {}
                    private void privateMethod() {}
                    void test() {
                        this.<cursor>
                    }
                }
                """,
                context -> List.of(
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
        assertSuggestions("""
                class Class {
                    public int publicField;
                    protected int protectedField;
                    private int privateField;
                    public void publicMethod() {}
                    protected void protectedMethod() {}
                    private void privateMethod() {}
                }
                new Class().<cursor>
                """,
                context -> List.of(
                        PropertySuggestion.getInstance(context, "Class", "publicField"),
                        MethodSuggestion.getInstance(context, "Class", "publicMethod")));
    }

    @Test
    public void inheritedVisibilityMembersTest() {
        assertSuggestions("""
                class Base {
                    public int publicField;
                    protected int protectedField;
                    private int privateField;
                    public void publicMethod() {}
                    protected void protectedMethod() {}
                    private void privateMethod() {}
                }
                class Child : Base {
                    void test() {
                        this.<cursor>
                    }
                }
                """,
                context -> List.of(
                        PropertySuggestion.getInstance(context, "Child", "publicField"),
                        PropertySuggestion.getInstance(context, "Child", "protectedField"),
                        MethodSuggestion.getInstance(context, "Child", "test"),
                        MethodSuggestion.getInstance(context, "Child", "publicMethod"),
                        MethodSuggestion.getInstance(context, "Child", "protectedMethod")));
    }

    @Test
    public void protectedMembersTest1() {
        assertSuggestions("""
                class Class : Java<com.zergatul.scripting.tests.completion.ClassTests$TestClass> {
                    constructor() {
                        <cursor>
                    }
                }
                """,
                context -> Lists.of(
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
        assertSuggestions("""
                class Class : Java<com.zergatul.scripting.tests.completion.ClassTests$TestClass> {
                    constructor() {
                        base.<cursor>
                    }
                }
                """,
                context -> List.of(
                        MethodSuggestion.getInstance(context, "Class", "method")));
    }

    @Test
    public void protectedMembersTest3() {
        assertSuggestions("""
                class Class : Java<com.zergatul.scripting.tests.completion.ClassTests$TestClass> {
                    constructor() {
                        this.<cursor>
                    }
                }
                """,
                context -> List.of(
                        PropertySuggestion.getInstance(context, "Class", "field"),
                        MethodSuggestion.getInstance(context, "Class", "method")));
    }

    @Test
    public void protectedMembersOnCapturedThisInLambdaTest() {
        assertSuggestions("""
                typealias Run = Java<com.zergatul.scripting.tests.compiler.helpers.Run>;

                class Class : Java<com.zergatul.scripting.tests.completion.ClassTests$TestClass> {
                    void execute() {
                        let run = new Run();
                        let self = this;
                        run.once(() => self.<cursor>);
                    }
                }
                """,
                context -> List.of(
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