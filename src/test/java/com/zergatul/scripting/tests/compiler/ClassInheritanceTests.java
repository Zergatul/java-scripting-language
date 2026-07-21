package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.parser.ParserErrors;
import com.zergatul.scripting.tests.framework.ComparatorCompilationParameters;
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
        String code = """
                class ClassA {
                    constructor(int x) {}
                }
                class ⟦ClassB⟧ : ClassA {}
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseClassNoParameterlessConstructor);
    }

    @Test
    public void baseFieldInheritedFieldTest() {
        String code = """
                class ClassA {
                    int value;
                }
                class ClassB : ClassA {
                    int ⟦value⟧;
                }
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseClassAlreadyHasMember);
    }

    @Test
    public void baseFieldInheritedMethodTest() {
        String code = """
                class ClassA {
                    int value;
                }
                class ClassB : ClassA {
                    void ⟦value⟧(){}
                }
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseClassAlreadyHasMember);
    }

    @Test
    public void baseMethodInheritedFieldTest() {
        String code = """
                class ClassA {
                    void value() {}
                }
                class ClassB : ClassA {
                    int ⟦value⟧;
                }
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseClassAlreadyHasMember);
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
        String code = """
                class ClassA {
                    void value() {}
                }
                class ClassB : ClassA {
                    int ⟦value⟧() => 1;
                }
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.MethodOverrideReturnMismatch);
    }

    @Test
    public void missingOverrideModifierTest() {
        String code = """
                class ClassA {
                    void value() {}
                }
                class ClassB : ClassA {
                    void ⟦value⟧() {}
                }
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.OverrideMissing);
    }

    @Test
    public void missingVirtualModifierTest() {
        String code = """
                class ClassA {
                    void value() {}
                }
                class ClassB : ClassA {
                    override void ⟦value⟧() {}
                }
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.NonVirtualOverride);
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
        String code = """
                let list = ⟦new Java<java.util.AbstractList>()⟧;
                """;

        comparator.assertDiagnostics(ApiRoot.class, code,
                new MarkedDiagnostic("⟦⟧", BinderErrors.CannotInstantiateAbstractClass),
                new MarkedDiagnostic("⟦⟧", BinderErrors.NoOverloadedConstructors, "Java<java.util.AbstractList>", 0, "No candidates"));
    }

    @Test
    public void cannotInstantiateInterfaceTest() {
        String code = """
                let list = ⟦new Java<java.util.List>()⟧;
                """;

        comparator.assertDiagnostics(ApiRoot.class, code,
                new MarkedDiagnostic("⟦⟧", BinderErrors.CannotInstantiateAbstractClass),
                new MarkedDiagnostic("⟦⟧", BinderErrors.NoOverloadedConstructors, "Java<java.util.List>", 0, "No candidates"));
    }

    @Test
    public void baseInExtensionTest() {
        String code = """
                extension(int) {
                    void method() => ⟦base⟧.toString();
                }
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseInvalidContext);
    }

    @Test
    public void cannotUseBaseAsValueTest() {
        String code = """
                class Class {
                    void method() {
                        let x = ⟦base⟧;
                    }
                }
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseInvalidUse);
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
        String code = """
                class Class {
                    override void ⟦notify⟧() {
                        base.notify();
                    }
                }
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.NonVirtualOverride);
    }

    @Test
    public void javaInterfaceImplementationMissingMethodTest() {
        String code = """
                class ⟦Class⟧ : Java<java.lang.Runnable> {}
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.MissingInheritedMethodImplementation, "run");
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
        String code = """
                class Class : Java<java.util.ArrayList>, ⟦Java<java.util.Vector>⟧ {}
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.MultipleBaseClasses);
    }

    @Test
    public void javaAbstractClassMissingMethodTest() {
        String code = """
                class ⟦Class⟧ : AbstractBase {}
                """;

        comparator.assertDiagnostics(
                new ComparatorCompilationParameters.Builder().api(ApiRoot.class).customType(AbstractBase.class).build(),
                code, "⟦⟧",
                BinderErrors.MissingInheritedMethodImplementation,
                "value");
    }

    @Test
    public void javaAbstractClassImplementationRequiresOverrideTest() {
        String code = """
                class Class : AbstractBase {
                    int ⟦value⟧() => 1;
                }
                """;

        comparator.assertDiagnostics(
                new ComparatorCompilationParameters.Builder().api(ApiRoot.class).customType(AbstractBase.class).build(),
                code, "⟦⟧",
                BinderErrors.OverrideMissing);
    }

    @Test
    public void abstractMethodNotSupportedTest() {
        String code = """
                class Class {
                    abstract void ⟦run⟧() {}
                }
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.AbstractMethodNotSupported);
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
    public void protectedJavaConstructorExplicitBaseCallTest() {
        String code = """
                class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedConstructorBase> {
                    constructor(string value) : base(value) {}
                }

                let instance = new Class("aa");
                stringStorage.add(instance.getValue());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of("aa"), ApiRoot.stringStorage.list);
    }

    @Test
    public void protectedJavaConstructorImplicitBaseCallTest() {
        String code = """
                class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedConstructorBase> {
                    constructor() {}
                }

                let instance = new Class();
                stringStorage.add(instance.getValue());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of("default"), ApiRoot.stringStorage.list);
    }

    @Test
    public void protectedJavaConstructorSynthesizedBaseCallTest() {
        String code = """
                class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedConstructorBase> {}

                let instance = new Class();
                stringStorage.add(instance.getValue());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of("default"), ApiRoot.stringStorage.list);
    }

    @Test
    public void protectedJavaConstructorObjectCreationTest() {
        String code = """
                let instance = ⟦new Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedConstructorBase>("aa")⟧;
                """;

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.NoOverloadedConstructors,
                "Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedConstructorBase>",
                1,
                "No candidates");
    }

    @Test
    public void confuseBaseAndThisConstructorTest() {
        String code = """
                class ClassA {
                    constructor(string value) {}
                }
                class ClassB : ClassA {
                    constructor() : base("default") {}
                    constructor(string value) : ⟦base()⟧ {}
                }
                """;

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.NoOverloadedConstructors,
                "ClassA",
                0,
                """
                Candidates:
                constructor ClassA(string value)""");
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
                constructor ClassA()
                constructor ClassA(int value)""";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.ConstructorInvalidArguments,
                "ClassA", candidates);
    }

    @Test
    public void noDefaultConstructorTest1() {
        String code = """
                class ClassA {
                    constructor(int x) {}
                }
                class ⟦ClassB⟧ : ClassA {}
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseClassNoParameterlessConstructor);
    }

    @Test
    public void noDefaultConstructorTest2() {
        String code = """
                class ClassA {
                    constructor(int x) {}
                }
                class ClassB : ClassA {
                    ⟦constructor⟧(int x) {}
                }
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseClassNoParameterlessConstructor);
    }

    @Test
    public void selfInheritTest() {
        String code = """
                class ClassA : ⟦ClassA⟧ {}
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.ClassCircularInheritance);
    }

    @Test
    public void inheritanceLoopTest() {
        String code = """
                class ClassA : ClassB {}
                class ClassB : ⟦ClassA⟧ {}
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.ClassCircularInheritance);
    }

    @Test
    public void inheritVoidTest() {
        String code = """
                class Class : ⟦void⟧ ⟪{⟫}
                """;

        comparator.assertDiagnostics(ApiRoot.class, code,
                new MarkedDiagnostic("⟦⟧", ParserErrors.TypeExpected, "void"),
                new MarkedDiagnostic("⟦⟧", ParserErrors.OpenCurlyBracketExpected, "void"),
                new MarkedDiagnostic("⟪⟫", ParserErrors.IdentifierExpected, "{"));
    }

    @Test
    public void inheritIntTest() {
        String code = """
                class ⟦Class⟧ : int {}
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseClassNoParameterlessConstructor);
    }

    @Test
    public void inheritFuncTest() {
        String code = """
                class ⟦Class⟧ : fn<int => int> {}
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseClassNoParameterlessConstructor);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static ObjectStorage objectStorage;
    }

    @SuppressWarnings("unused")
    public static class ProtectedConstructorBase {

        private final String value;

        protected ProtectedConstructorBase() {
            this.value = "default";
        }

        protected ProtectedConstructorBase(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @CustomType(name = "AbstractBase")
    public static abstract class AbstractBase {
        public abstract int value();
    }
}