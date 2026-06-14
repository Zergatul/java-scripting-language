package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.parser.ParserErrors;
import com.zergatul.scripting.tests.utility.MarkedCode;
import com.zergatul.scripting.tests.utility.MarkedDiagnostic;
import com.zergatul.scripting.type.CustomType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.ObjectStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class ClassInheritanceTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.objectStorage = new ObjectStorage();
    }

    @Test
    public void simpleTest() {
        String code = """
                class ClassA {
                    int value;
                    constructor() {
                        value = 123;
                    }
                }
                class ClassB : ClassA {}
                
                let c = new ClassB();
                intStorage.add(c.value);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void baseClassSortingTest() {
        String code = """
                class ClassB : ClassA {}
                class ClassA {
                    int value;
                    constructor() {
                        value = 123;
                    }
                }
                
                let c = new ClassB();
                intStorage.add(c.value);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void noBaseDefaultConstructorTest() {
        MarkedCode marked = MarkedCode.from("""
                class ClassA {
                    constructor(int x) {}
                }
                class ⟦ClassB⟧ : ClassA {}
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.BaseClassNoParameterlessConstructor);
    }

    @Test
    public void baseFieldInheritedFieldTest() {
        MarkedCode marked = MarkedCode.from("""
                class ClassA {
                    int value;
                }
                class ClassB : ClassA {
                    int ⟦value⟧;
                }
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.BaseClassAlreadyHasMember);
    }

    @Test
    public void baseFieldInheritedMethodTest() {
        MarkedCode marked = MarkedCode.from("""
                class ClassA {
                    int value;
                }
                class ClassB : ClassA {
                    void ⟦value⟧(){}
                }
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.BaseClassAlreadyHasMember);
    }

    @Test
    public void baseMethodInheritedFieldTest() {
        MarkedCode marked = MarkedCode.from("""
                class ClassA {
                    void value() {}
                }
                class ClassB : ClassA {
                    int ⟦value⟧;
                }
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.BaseClassAlreadyHasMember);
    }

    @Test
    public void sameMethodNameDifferentParametersTest() {
        String code = """
                class ClassA {
                    void method() => intStorage.add(10);
                }
                class ClassB : ClassA {
                    void method(int x) => intStorage.add(x);
                }
                
                let instance = new ClassB();
                instance.method();
                instance.method(11);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(10, 11), ApiRoot.intStorage.list);
    }

    @Test
    public void differentReturnTypesOverrideModifierTest() {
        MarkedCode marked = MarkedCode.from("""
                class ClassA {
                    void value() {}
                }
                class ClassB : ClassA {
                    int ⟦value⟧() => 1;
                }
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.MethodOverrideReturnMismatch);
    }

    @Test
    public void missingOverrideModifierTest() {
        MarkedCode marked = MarkedCode.from("""
                class ClassA {
                    void value() {}
                }
                class ClassB : ClassA {
                    void ⟦value⟧() {}
                }
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.OverrideMissing);
    }

    @Test
    public void missingVirtualModifierTest() {
        MarkedCode marked = MarkedCode.from("""
                class ClassA {
                    void value() {}
                }
                class ClassB : ClassA {
                    override void ⟦value⟧() {}
                }
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.NonVirtualOverride);
    }

    @Test
    public void simpleOverrideTest() {
        String code = """
                class ClassA {
                    virtual int value() => 1;
                }
                class ClassB : ClassA {
                    override int value() => 2;
                }
                
                intStorage.add(new ClassA().value());
                intStorage.add(new ClassB().value());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
    }

    @Test
    public void baseCallOverrideTest() {
        String code = """
                class ClassA {
                    virtual int value() => 1;
                }
                class ClassB : ClassA {
                    override int value() => base.value() + 1;
                }
                
                intStorage.add(new ClassA().value());
                intStorage.add(new ClassB().value());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
    }

    @Test
    public void baseMethodInvalidArgumentsTest() {
        String code = """
                class ClassA {
                    virtual void method(int x) {}
                }
                class ClassB : ClassA {
                    override void method(int x) {}
                    void test() => base.method⟦("text")⟧;
                }
                """;

        String candidates = """
                Candidates:
                void method(int x)""";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.MethodInvalidArguments,
                "method", candidates);
    }

    @Test
    public void baseMethodArgumentCountMismatchTest() {
        String code = """
                class ClassA {
                    virtual void method(int x) {}
                }
                class ClassB : ClassA {
                    override void method(int x) {}
                    void test() => base.⟦method⟧();
                }
                """;

        String candidates = """
                Candidates:
                void method(int x)""";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.NoOverloadedMethods,
                "method", 0, candidates);
    }

    @Test
    public void methodsShouldBeFinalByDefault() throws Exception {
        String code = """
                class Class {
                    void method(){}
                }
                
                objectStorage.add(new Class());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertEquals(1, ApiRoot.objectStorage.list.size());

        Method method = ApiRoot.objectStorage.list.getFirst().getClass().getMethod("method");
        Assertions.assertTrue(Modifier.isFinal(method.getModifiers()));
    }

    @Test
    public void baseClassAssignTest() {
        String code = """
                class ClassA {
                    virtual int value() => 1;
                }
                class ClassB : ClassA {
                    override int value() => 2;
                }
                
                ClassA variable = new ClassA();
                intStorage.add(variable.value());
                variable = new ClassB();
                intStorage.add(variable.value());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
    }

    @Test
    public void javaTypeInheritTest() {
        String code = """
                class MyList : Java<java.util.Vector> {
                    void add(int value) => base.add(value);
                    int get2(int index) => base.get(index) as int;
                }

                let list = new MyList();
                list.add(5);
                list.add(6);
                list.add(7);
                for (int i = 0; i < list.size(); i++) {
                    intStorage.add(list.get2(i));
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(5, 6, 7), ApiRoot.intStorage.list);
    }

    @Test
    public void cannotInstantiateAbstractClassTest() {
        MarkedCode marked = MarkedCode.from("""
                let list = ⟦new Java<java.util.AbstractList>()⟧;
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked,
                new MarkedDiagnostic("⟦⟧", BinderErrors.CannotInstantiateAbstractClass),
                new MarkedDiagnostic("⟦⟧", BinderErrors.NoOverloadedConstructors, "Java<java.util.AbstractList>", 0));
    }

    @Test
    public void cannotInstantiateInterfaceTest() {
        MarkedCode marked = MarkedCode.from("""
                let list = ⟦new Java<java.util.List>()⟧;
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked,
                new MarkedDiagnostic("⟦⟧", BinderErrors.CannotInstantiateAbstractClass),
                new MarkedDiagnostic("⟦⟧", BinderErrors.NoOverloadedConstructors, "Java<java.util.List>", 0));
    }

    @Test
    public void baseInExtensionTest() {
        MarkedCode marked = MarkedCode.from("""
                extension(int) {
                    void method() => ⟦base⟧.toString();
                }
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.BaseInvalidContext);
    }

    @Test
    public void cannotUseBaseAsValueTest() {
        MarkedCode marked = MarkedCode.from("""
                class Class {
                    void method() {
                        let x = ⟦base⟧;
                    }
                }
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.BaseInvalidUse);
    }

    @Test
    public void overrideJavaClassMethodTest() {
        String code = """
                class MyList : Java<java.util.Vector> {
                    override int size() => 12;
                }
                
                let list = new MyList();
                intStorage.add(list.size());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(12), ApiRoot.intStorage.list);
    }

    @Test
    public void cannotOverrideFinalMethodTest() {
        MarkedCode marked = MarkedCode.from("""
                class Class {
                    override void ⟦notify⟧() {
                        base.notify();
                    }
                }
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.NonVirtualOverride);
    }

    @Test
    public void javaInterfaceImplementationMissingMethodTest() {
        MarkedCode marked = MarkedCode.from("""
                class ⟦Class⟧ : Java<java.lang.Runnable> {}
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.MissingInheritedMethodImplementation, "run");
    }

    @Test
    public void javaInterfaceImplementationTest() {
        String code = """
                class Class : Java<java.lang.Runnable> {
                    override void run() {
                        intStorage.add(123);
                    }
                }
                
                let instance = new Class();
                Java<java.lang.Runnable> runnable = instance;
                runnable.run();
                objectStorage.add(instance);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(123), ApiRoot.intStorage.list);
        Assertions.assertTrue(ApiRoot.objectStorage.list.getFirst() instanceof Runnable);
    }

    @Test
    public void javaMultipleInterfaceImplementationTest() {
        String code = """
                class Class : Java<java.lang.Runnable>, Java<java.lang.AutoCloseable> {
                    override void run() {
                        intStorage.add(1);
                    }
                    override void close() {
                        intStorage.add(2);
                    }
                }
                
                let instance = new Class();
                instance.run();
                instance.close();
                objectStorage.add(instance);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
        Object object = ApiRoot.objectStorage.list.getFirst();
        Assertions.assertTrue(object instanceof Runnable);
        Assertions.assertTrue(object instanceof AutoCloseable);
    }

    @Test
    public void javaClassAndInterfaceImplementationTest() {
        String code = """
                class Class : Java<java.util.ArrayList>, Java<java.lang.Runnable> {
                    override void run() {
                        intStorage.add(this.size());
                    }
                }
                
                let instance = new Class();
                instance.add(10);
                instance.add(20);
                instance.run();
                objectStorage.add(instance);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(2), ApiRoot.intStorage.list);
        Object object = ApiRoot.objectStorage.list.getFirst();
        Assertions.assertTrue(object instanceof java.util.ArrayList);
        Assertions.assertTrue(object instanceof Runnable);
        Assertions.assertIterableEquals(List.of(10, 20), (List<?>) object);
    }

    @Test
    public void multipleJavaBaseClassesTest() {
        MarkedCode marked = MarkedCode.from("""
                class Class : Java<java.util.ArrayList>, ⟦Java<java.util.Vector>⟧ {}
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.MultipleBaseClasses);
    }

    @Test
    public void javaAbstractClassMissingMethodTest() {
        MarkedCode marked = MarkedCode.from("""
                class ⟦Class⟧ : AbstractBase {}
                """);

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.MissingInheritedMethodImplementation, marked.getRange("⟦⟧"), "value")),
                getDiagnostics(ApiRoot.class, marked.getCode(), AbstractBase.class));
    }

    @Test
    public void javaAbstractClassImplementationRequiresOverrideTest() {
        MarkedCode marked = MarkedCode.from("""
                class Class : AbstractBase {
                    int ⟦value⟧() => 1;
                }
                """);

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.OverrideMissing, marked.getRange("⟦⟧"))),
                getDiagnostics(ApiRoot.class, marked.getCode(), AbstractBase.class));
    }

    @Test
    public void abstractMethodNotSupportedTest() {
        MarkedCode marked = MarkedCode.from("""
                class Class {
                    abstract void ⟦run⟧() {}
                }
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.AbstractMethodNotSupported);
    }

    @Test
    public void constructorInitializerBaseSimpleTest() {
        String code = """
                class ClassA {
                    int x;
                    constructor(int value) {
                        x = value;
                    }
                }
                class ClassB : ClassA {
                    constructor(int value1, int value2) : base(value1 + value2) {}
                }
                
                let instance = new ClassB(10, 4);
                intStorage.add(instance.x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(14), ApiRoot.intStorage.list);
    }

    @Test
    public void constructorInitializerBaseInvalidArgumentsTest() {
        String code = """
                class ClassA {
                    constructor(int value) {}
                }
                class ClassB : ClassA {
                    constructor() : base⟦("text")⟧ {}
                }
                """;

        String candidates = """
                Candidates:
                constructor ClassA(int value)""";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.ConstructorInvalidArguments,
                "ClassA", candidates);
    }

    @Test
    public void constructorInitializerThisSimpleTest() {
        String code = """
                class ClassA {
                    int x;
                    constructor(int value) {
                        x = value;
                    }
                    constructor(int value1, int value2) : this(value1 + value2) {}
                }

                let instance = new ClassA(10, 4);
                intStorage.add(instance.x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(14), ApiRoot.intStorage.list);
    }

    @Test
    public void constructorInitializerThisInvalidArgumentsTest() {
        String code = """
                class ClassA {
                    constructor(int value) {}
                    constructor() : this⟦("text")⟧ {}
                }
                """;

        String candidates = """
                Candidates:
                constructor ClassA(int value)
                constructor ClassA()""";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.ConstructorInvalidArguments,
                "ClassA", candidates);
    }

    @Test
    public void noDefaultConstructorTest1() {
        MarkedCode marked = MarkedCode.from("""
                class ClassA {
                    constructor(int x) {}
                }
                class ⟦ClassB⟧ : ClassA {}
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.BaseClassNoParameterlessConstructor);
    }

    @Test
    public void noDefaultConstructorTest2() {
        MarkedCode marked = MarkedCode.from("""
                class ClassA {
                    constructor(int x) {}
                }
                class ClassB : ClassA {
                    ⟦constructor⟧(int x) {}
                }
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.BaseClassNoParameterlessConstructor);
    }

    @Test
    public void selfInheritTest() {
        MarkedCode marked = MarkedCode.from("""
                class ClassA : ⟦ClassA⟧ {}
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.ClassCircularInheritance);
    }

    @Test
    public void inheritanceLoopTest() {
        MarkedCode marked = MarkedCode.from("""
                class ClassA : ClassB {}
                class ClassB : ⟦ClassA⟧ {}
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked, "⟦⟧", BinderErrors.ClassCircularInheritance);
    }

    @Test
    public void inheritVoidTest() {
        MarkedCode marked = MarkedCode.from("""
                class Class : ⟦void⟧ ⟪{⟫}
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked,
                new MarkedDiagnostic("⟦⟧", ParserErrors.TypeExpected, "void"),
                new MarkedDiagnostic("⟦⟧", ParserErrors.OpenCurlyBracketExpected, "void"),
                new MarkedDiagnostic("⟪⟫", ParserErrors.IdentifierExpected, "{"));
    }

    @Test
    public void inheritIntTest() {
        MarkedCode marked = MarkedCode.from("""
                class ⟦Class⟧ : int {}
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked,
                new MarkedDiagnostic("⟦⟧", BinderErrors.BaseClassNoParameterlessConstructor));
    }

    @Test
    public void inheritFuncTest() {
        MarkedCode marked = MarkedCode.from("""
                class ⟦Class⟧ : fn<int => int> {}
                """);

        comparator.assertDiagnostics(ApiRoot.class, marked,
                new MarkedDiagnostic("⟦⟧", BinderErrors.BaseClassNoParameterlessConstructor));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static ObjectStorage objectStorage;
    }

    @CustomType(name = "AbstractBase")
    public static abstract class AbstractBase {
        public abstract int value();
    }
}