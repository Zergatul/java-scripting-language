package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
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

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123));
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

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123));
    }

    @Test
    public void noBaseDefaultConstructorTest() {
        String code = """
                class ClassA {
                    constructor(int x) {}
                }
                class ClassB : ClassA {}
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.BaseClassNoParameterlessConstructor, new SingleLineTextRange(4, 7, 49, 6))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void baseFieldInheritedFieldTest() {
        String code = """
                class ClassA {
                    int value;
                }
                class ClassB : ClassA {
                    int value;
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.BaseClassAlreadyHasMember, new SingleLineTextRange(5, 9, 64, 5))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void baseFieldInheritedMethodTest() {
        String code = """
                class ClassA {
                    int value;
                }
                class ClassB : ClassA {
                    void value(){}
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.BaseClassAlreadyHasMember, new SingleLineTextRange(5, 10, 65, 5))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void baseMethodInheritedFieldTest() {
        String code = """
                class ClassA {
                    void value() {}
                }
                class ClassB : ClassA {
                    int value;
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.BaseClassAlreadyHasMember, new SingleLineTextRange(5, 9, 69, 5))),
                getDiagnostics(ApiRoot.class, code));
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

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 11));
    }

    @Test
    public void differentReturnTypesOverrideModifierTest() {
        String code = """
                class ClassA {
                    void value() {}
                }
                class ClassB : ClassA {
                    int value() => 1;
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.MethodOverrideReturnMismatch, new SingleLineTextRange(5, 9, 69, 5))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void missingOverrideModifierTest() {
        String code = """
                class ClassA {
                    void value() {}
                }
                class ClassB : ClassA {
                    void value() {}
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.OverrideMissing, new SingleLineTextRange(5, 10, 70, 5))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void missingVirtualModifierTest() {
        String code = """
                class ClassA {
                    void value() {}
                }
                class ClassB : ClassA {
                    override void value() {}
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.NonVirtualOverride, new SingleLineTextRange(5, 19, 79, 5))),
                getDiagnostics(ApiRoot.class, code));
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

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2));
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

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2));
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

        Assertions.assertEquals(ApiRoot.objectStorage.list.size(), 1);

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

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2));
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

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(5, 6, 7));
    }

    @Test
    public void cannotInstantiateAbstractClassTest() {
        String code = """
                let list = new Java<java.util.AbstractList>();
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.CannotInstantiateAbstractClass, new SingleLineTextRange(1, 12, 11, 34)),
                        new DiagnosticMessage(BinderErrors.NoOverloadedConstructors, new SingleLineTextRange(1, 12, 11, 34), "Java<java.util.AbstractList>", 0)),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void cannotInstantiateInterfaceTest() {
        String code = """
                let list = new Java<java.util.List>();
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.CannotInstantiateAbstractClass, new SingleLineTextRange(1, 12, 11, 26)),
                        new DiagnosticMessage(BinderErrors.NoOverloadedConstructors, new SingleLineTextRange(1, 12, 11, 26), "Java<java.util.List>", 0)),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void baseInExtensionTest() {
        String code = """
                extension(int) {
                    void method() => base.toString();
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.BaseInvalidContext, new SingleLineTextRange(2, 22, 38, 4))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void cannotUseBaseAsValueTest() {
        String code = """
                class Class {
                    void method() {
                        let x = base;
                    }
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.BaseInvalidUse, new SingleLineTextRange(3, 17, 50, 4))),
                getDiagnostics(ApiRoot.class, code));
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

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(12));
    }

    @Test
    public void cannotOverrideFinalMethodTest() {
        String code = """
                class Class {
                    override void notify() {
                        base.notify();
                    }
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.NonVirtualOverride, new SingleLineTextRange(2, 19, 32, 6))),
                getDiagnostics(ApiRoot.class, code));
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

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(14));
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

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(14));
    }

    @Test
    public void noDefaultConstructorTest1() {
        String code = """
                class ClassA {
                    constructor(int x) {}
                }
                class ClassB : ClassA {}
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.BaseClassNoParameterlessConstructor, new SingleLineTextRange(4, 7, 49, 6))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void noDefaultConstructorTest2() {
        String code = """
                class ClassA {
                    constructor(int x) {}
                }
                class ClassB : ClassA {
                    constructor(int x) {}
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.BaseClassNoParameterlessConstructor, new SingleLineTextRange(5, 5, 71, 11))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void selfInheritTest() {
        String code = """
                class ClassA : ClassA {}
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.ClassCircularInheritance, new SingleLineTextRange(1, 16, 15, 6))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void inheritanceLoopTest() {
        String code = """
                class ClassA : ClassB {}
                class ClassB : ClassA {}
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.ClassCircularInheritance, new SingleLineTextRange(2, 16, 40, 6))),
                getDiagnostics(ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static ObjectStorage objectStorage;
    }
}