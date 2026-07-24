package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.parser.ParserErrors;
import com.zergatul.scripting.tests.framework.ComparatorCompilationParameters;
import com.zergatul.scripting.tests.utility.MarkedDiagnostic;
import com.zergatul.scripting.type.CustomType;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.ObjectStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static org.objectweb.asm.Opcodes.*;

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
    public void protectedJavaMethodOverrideTest() throws Exception {
        String code = """
                class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedOverrideBase> {
                    protected override int transform(int value) => value + 1;
                }

                let instance = new Class();
                intStorage.add(instance.invoke(10));
                objectStorage.add(instance);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(11), ApiRoot.intStorage.list);
        Method method = ApiRoot.objectStorage.list.getFirst().getClass().getDeclaredMethod("transform", int.class);
        Assertions.assertTrue(Modifier.isProtected(method.getModifiers()));
    }

    @Test
    public void protectedScriptConstructorTest() throws Exception {
        String code = """
                class Base {
                    protected int value;
                    protected constructor(int value) {
                        this.value = value;
                    }
                }
                class Child : Base {
                    public constructor() : base(17) {}
                    public int getValue() => value;
                }

                let instance = new Child();
                intStorage.add(instance.getValue());
                objectStorage.add(instance);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(17), ApiRoot.intStorage.list);
        Class<?> baseClass = ApiRoot.objectStorage.list.getFirst().getClass().getSuperclass();
        Assertions.assertTrue(Modifier.isProtected(baseClass.getDeclaredConstructor(int.class).getModifiers()));
    }

    @Test
    public void protectedScriptMembersOnSubclassReceiverTest() {
        String code = """
                class Base {
                    protected int value;

                    protected void setValue(int value) {
                        this.value = value;
                    }
                }
                class Child : Base {
                    public void copyFrom(Child other) {
                        other.setValue(43);
                        value = other.value;
                    }

                    public int getValue() => value;
                }

                let instance = new Child();
                instance.copyFrom(new Child());
                intStorage.add(instance.getValue());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(43), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedScriptMemberOnBaseReceiverTest() {
        String code = """
                class Base {
                    protected int value;
                }
                class Child : Base {
                    public int read(Base other) => other.⟦value⟧;
                }
                """;

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MemberDoesNotExist,
                "Base",
                "value");
    }

    @Test
    public void protectedScriptMemberOnSiblingReceiverTest() {
        String code = """
                class Base {
                    protected void method() {}
                }
                class First : Base {
                    public void call(Second other) => other.⟦method⟧();
                }
                class Second : Base {}
                """;

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MemberDoesNotExist,
                "Second",
                "method");
    }

    @Test
    public void publicOverrideCanWidenProtectedMethodVisibilityTest() throws Exception {
        String code = """
                class Base {
                    protected virtual int getValue() => 47;
                }
                class Child : Base {
                    public override int getValue() => base.getValue() + 1;
                }

                let instance = new Child();
                intStorage.add(instance.getValue());
                objectStorage.add(instance);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(48), ApiRoot.intStorage.list);
        Method method = ApiRoot.objectStorage.list.getFirst().getClass().getDeclaredMethod("getValue");
        Assertions.assertTrue(Modifier.isPublic(method.getModifiers()));
    }

    @Test
    public void privateBaseConstructorCannotBeCalledExplicitlyTest() {
        String code = """
                class Base {
                    private constructor(int value) {}
                }
                class Child : Base {
                    constructor() : ⟦base(1)⟧ {}
                }
                """;

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.NoConstructors,
                "Base");
    }

    @Test
    public void cannotReducePublicMethodVisibilityTest() {
        String code = """
                class Base {
                    public virtual void method() {}
                }
                class Child : Base {
                    ⟦protected⟧ override void method() {}
                }
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.CannotReduceMethodVisibility);
    }

    @Test
    public void cannotReduceProtectedMethodVisibilityTest() {
        String code = """
                class Base {
                    protected virtual void method() {}
                }
                class Child : Base {
                    ⟦private⟧ override void method() {}
                }
                """;

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.CannotReduceMethodVisibility);
    }

    @Test
    public void privateBaseMembersCanBeRedeclaredTest() {
        String code = """
                class Base {
                    private int value;
                    private int method() => 1;
                    public int getBaseValue() => value + method();
                }
                class Child : Base {
                    public int value;
                    public int method() => 2;
                }

                let instance = new Child();
                instance.value = 3;
                intStorage.add(instance.getBaseValue());
                intStorage.add(instance.method());
                intStorage.add(instance.value);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(1, 2, 3), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedMethodDoesNotImplementPublicInterfaceMethodTest() {
        String code = """
                class Base {
                    protected virtual void run() {}
                }
                class ⟦Child⟧ : Base, Java<java.lang.Runnable> {}
                """;

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MissingInheritedMethodImplementation,
                "run");
    }

    @Test
    public void protectedJavaMethodBaseCallTest1() {
        String code = """
                class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedMethodBase> {
                    constructor() {
                        base.add(123);
                    }
                }

                new Class();
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaMethodBaseCallTest2() {
        String code = """
                class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedMethodBase> {
                    constructor() {
                        this.add(123);
                    }
                }

                new Class();
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaMethodBaseCallTest3() {
        String code = """
                class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedMethodBase> {
                    constructor() {
                        add(123);
                    }
                }

                new Class();
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaMethodOnCapturedThisInLambdaTest() {
        String code = """
                typealias Run = Java<com.zergatul.scripting.tests.compiler.helpers.Run>;

                class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedMethodBase> {
                    void execute() {
                        let run = new Run();
                        let self = this;
                        run.once(() => {
                            self.value = 100;
                            self.value += 23;
                            self.add(self.value);
                        });
                    }
                }

                new Class().execute();
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedScriptMembersOnSubclassReceiverInLambdaTest() {
        String code = """
                typealias Run = Java<com.zergatul.scripting.tests.compiler.helpers.Run>;

                class Base {
                    protected int value;

                    protected void setValue(int value) {
                        this.value = value;
                    }
                }
                class Child : Base {
                    public void copyFrom(Child other) {
                        let self = this;
                        new Run().once(() => {
                            other.setValue(61);
                            self.value = other.value;
                        });
                    }

                    public int getValue() => value;
                }

                let instance = new Child();
                instance.copyFrom(new Child());
                intStorage.add(instance.getValue());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(61), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedScriptMemberOnBaseReceiverInLambdaTest() {
        String code = """
                typealias Run = Java<com.zergatul.scripting.tests.compiler.helpers.Run>;

                class Base {
                    protected int value;
                }
                class Child : Base {
                    public void read(Base other) {
                        new Run().once(() => intStorage.add(other.⟦value⟧));
                    }
                }
                """;

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MemberDoesNotExist,
                "Base",
                "value");
    }

    @Test
    public void protectedJavaMethodFromJdkModuleTest() {
        String code = """
                class Class : Java<java.util.Vector> {
                    constructor() {
                        base.add(1);
                        base.add(2);
                        base.removeRange(0, 1);
                        intStorage.add(base.size());
                    }
                }

                new Class();
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(1), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaMethodOnSubclassReceiverTest() {
        String code = """
                class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedMethodBase> {
                    void call(Class other) {
                        other.add(321);
                    }
                }

                new Class().call(new Class());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(321), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaMethodOnBaseReceiverTest() {
        String code = """
                class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedMethodBase> {
                    void call(Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedMethodBase> other) {
                        other.⟦add⟧(321);
                    }
                }
                """;

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MemberDoesNotExist,
                SType.fromJavaType(ProtectedMethodBase.class),
                "add");
    }

    @Test
    public void protectedJavaFieldDirectAccessTest() {
        String code = """
                class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedFieldBase> {
                    constructor() {
                        value = 10;
                        this.value += 5;
                        value++;
                        intStorage.add(this.value);
                    }
                }

                new Class();
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(16), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaFieldOnSubclassReceiverTest() {
        String code = """
                class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedFieldBase> {
                    void call(Class other) {
                        other.value = 321;
                        intStorage.add(other.value);
                    }
                }

                new Class().call(new Class());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(321), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaFieldOnBaseReceiverTest() {
        String code = """
                class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedFieldBase> {
                    void call(Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedFieldBase> other) {
                        intStorage.add(other.⟦value⟧);
                    }
                }
                """;

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MemberDoesNotExist,
                SType.fromJavaType(ProtectedFieldBase.class),
                "value");
    }

    @Test
    public void protectedJavaFieldFromJdkModuleTest() {
        String code = """
                class Class : Java<java.io.ByteArrayInputStream> {
                    constructor() : base(new int8[0]) {
                        this.pos = 7;
                        intStorage.add(this.pos);
                    }
                }

                new Class();
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(7), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedStaticJavaFieldDirectAccessTest() {
        String code = """
                typealias Base = Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedFieldBase>;

                class Class : Base {
                    constructor() {
                        Base.staticValue = 10;
                        Base.staticValue++;
                        Base.staticValue += 5;
                        intStorage.add(Base.staticValue);
                    }
                }

                new Class();
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(16), ApiRoot.intStorage.list);
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
    public void syntheticJavaMethodImplementsInterfaceMethodTest() throws Exception {
        Class<?> baseClass = SyntheticMethodBaseHolder.TYPE;
        Method method = baseClass.getDeclaredMethod("value");

        Assertions.assertTrue(method.isSynthetic());
        Assertions.assertFalse(method.isBridge());
        Assertions.assertFalse(Modifier.isAbstract(method.getModifiers()));
        Assertions.assertEquals(SyntheticMethodContract.class, baseClass.getInterfaces()[0]);

        String code = """
                class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$SyntheticMethodBase> {}

                objectStorage.add(new Class());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Object object = ApiRoot.objectStorage.list.getFirst();
        Assertions.assertTrue(object instanceof SyntheticMethodContract);
        Assertions.assertEquals(123, ((SyntheticMethodContract) object).value());
    }

    @Test
    public void bridgeJavaMethodImplementsExplicitRawInterfaceMethodTest() {
        Method bridge = Arrays.stream(GenericValueBase.class.getDeclaredMethods())
                .filter(Method::isBridge)
                .findFirst()
                .orElseThrow();

        Assertions.assertTrue(bridge.isSynthetic());
        Assertions.assertFalse(Modifier.isAbstract(bridge.getModifiers()));
        Assertions.assertEquals(Object.class, bridge.getReturnType());

        String code = """
                class Class :
                    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$GenericValueBase>,
                    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$GenericValue> {}

                objectStorage.add(new Class());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Object object = ApiRoot.objectStorage.list.getFirst();
        Assertions.assertTrue(object instanceof GenericValue);
        Assertions.assertEquals("bridge", ((GenericValue<?>) object).value());
    }

    @Test
    public void javaDefaultInterfaceMethodDoesNotRequireImplementationTest() {
        String code = """
                class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$DefaultMethodInterface> {}

                objectStorage.add(new Class());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Object object = ApiRoot.objectStorage.list.getFirst();
        Assertions.assertTrue(object instanceof DefaultMethodInterface);
        Assertions.assertEquals(123, ((DefaultMethodInterface) object).value());
    }

    @Test
    public void moreSpecificDefaultInterfaceMethodImplementsAbstractBaseContractTest() {
        String code = """
                class Class :
                    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$AbstractInterfaceBase>,
                    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$MoreSpecificDefaultInterface> {}

                objectStorage.add(new Class());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Object object = ApiRoot.objectStorage.list.getFirst();
        Assertions.assertTrue(object instanceof AbstractMethodInterface);
        Assertions.assertEquals(123, ((AbstractMethodInterface) object).value());
    }

    @Test
    public void abstractClassMethodTakesPrecedenceOverDefaultInterfaceMethodTest() {
        String code = """
                class ⟦Class⟧ :
                    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$AbstractMethodBase>,
                    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$MoreSpecificDefaultInterface> {}
                """;

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MissingInheritedMethodImplementation,
                "value");
    }

    @Test
    public void conflictingDefaultInterfaceMethodsRequireImplementationTest() {
        String code = """
                class ⟦Class⟧ :
                    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$FirstDefaultMethodInterface>,
                    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$SecondDefaultMethodInterface> {}
                """;

        // we probably need another error for this
        // java compiler reports:
        // com.zergatul.scripting.tests.compiler.ClassInheritanceTests.TestClass inherits unrelated defaults for value() from types com.zergatul.scripting.tests.compiler.ClassInheritanceTests.FirstDefaultMethodInterface and com.zergatul.scripting.tests.compiler.ClassInheritanceTests.SecondDefaultMethodInterface
        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MissingInheritedMethodImplementation,
                "value");
    }

    @Test
    public void unrelatedDefaultMethodDoesNotImplementAbstractInterfaceMethodTest() {
        String code = """
                class ⟦Class⟧ :
                    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$AbstractMethodInterface>,
                    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$DefaultMethodInterface> {}
                """;

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MissingInheritedMethodImplementation,
                "value");
    }

    @Test
    public void abstractSubinterfaceMethodSuppressesParentDefaultMethodTest() {
        String code = """
                class ⟦Class⟧ :
                    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$DefaultMethodInterface>,
                    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$AbstractRedeclaringInterface> {}
                """;

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MissingInheritedMethodImplementation,
                "value");
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

    @SuppressWarnings("unused")
    public static class ProtectedMethodBase {

        protected int value;

        protected void add(int value) {
            ApiRoot.intStorage.add(value);
        }
    }

    @SuppressWarnings("unused")
    public static class ProtectedOverrideBase {

        public int invoke(int value) {
            return transform(value);
        }

        protected int transform(int value) {
            return value;
        }
    }

    @SuppressWarnings("unused")
    public static class ProtectedFieldBase {
        protected int value;
        protected static int staticValue;
    }

    @CustomType(name = "AbstractBase")
    public static abstract class AbstractBase {
        public abstract int value();
    }

    public interface SyntheticMethodContract {
        int value();
    }

    public interface GenericValue<T> {
        T value();
    }

    public static class GenericValueBase implements GenericValue<String> {
        @Override
        public String value() {
            return "bridge";
        }
    }

    public interface DefaultMethodInterface {
        default int value() {
            return 123;
        }
    }

    @SuppressWarnings("unused")
    public interface AbstractRedeclaringInterface extends DefaultMethodInterface {
        int value();
    }

    public interface AbstractMethodInterface {
        int value();
    }

    @SuppressWarnings("unused")
    public static abstract class AbstractInterfaceBase implements AbstractMethodInterface {}

    public static abstract class AbstractMethodBase implements AbstractMethodInterface {
        @Override
        public abstract int value();
    }

    @SuppressWarnings("unused")
    public interface MoreSpecificDefaultInterface extends AbstractMethodInterface {
        default int value() {
            return 123;
        }
    }

    public interface FirstDefaultMethodInterface {
        default int value() {
            return 1;
        }
    }

    public interface SecondDefaultMethodInterface {
        default int value() {
            return 2;
        }
    }

    private static class SyntheticMethodBaseHolder {
        private static final Class<?> TYPE = defineSyntheticMethodBase();
    }

    private static Class<?> defineSyntheticMethodBase() {
        String className = ClassInheritanceTests.class.getName() + "$SyntheticMethodBase";
        String internalName = className.replace('.', '/');

        ClassWriter writer = new ClassWriter(0);
        writer.visit(
                V21,
                ACC_PUBLIC | ACC_SUPER,
                internalName,
                null,
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(SyntheticMethodContract.class) });

        MethodVisitor constructor = writer.visitMethod(
                ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(
                INVOKESPECIAL,
                Type.getInternalName(Object.class),
                "<init>",
                "()V",
                false);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();

        MethodVisitor value = writer.visitMethod(
                ACC_PUBLIC | ACC_SYNTHETIC,
                "value",
                "()I",
                null,
                null);
        value.visitCode();
        value.visitIntInsn(BIPUSH, 123);
        value.visitInsn(IRETURN);
        value.visitMaxs(1, 1);
        value.visitEnd();

        writer.visitEnd();

        try {
            return MethodHandles.lookup().defineClass(writer.toByteArray());
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }
}