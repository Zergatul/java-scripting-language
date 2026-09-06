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
import com.zergatul.scripting.utility.Lists;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

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
        String code =
                "class ClassA {\n" +
                "    int value;\n" +
                "    constructor() {\n" +
                "        value = 123;\n" +
                "    }\n" +
                "}\n" +
                "class ClassB : ClassA {}\n" +
                "\n" +
                "let c = new ClassB();\n" +
                "intStorage.add(c.value);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void baseClassSortingTest() {
        String code =
                "class ClassB : ClassA {}\n" +
                "class ClassA {\n" +
                "    int value;\n" +
                "    constructor() {\n" +
                "        value = 123;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "let c = new ClassB();\n" +
                "intStorage.add(c.value);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void noBaseDefaultConstructorTest() {
        String code =
                "class ClassA {\n" +
                "    constructor(int x) {}\n" +
                "}\n" +
                "class ⟦ClassB⟧ : ClassA {}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseClassNoParameterlessConstructor);
    }

    @Test
    public void baseFieldInheritedFieldTest() {
        String code =
                "class ClassA {\n" +
                "    int value;\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    int ⟦value⟧;\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseClassAlreadyHasMember);
    }

    @Test
    public void baseFieldInheritedMethodTest() {
        String code =
                "class ClassA {\n" +
                "    int value;\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    void ⟦value⟧(){}\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseClassAlreadyHasMember);
    }

    @Test
    public void baseMethodInheritedFieldTest() {
        String code =
                "class ClassA {\n" +
                "    void value() {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    int ⟦value⟧;\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseClassAlreadyHasMember);
    }

    @Test
    public void sameMethodNameDifferentParametersTest() {
        String code =
                "class ClassA {\n" +
                "    void method() => intStorage.add(10);\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    void method(int x) => intStorage.add(x);\n" +
                "}\n" +
                "\n" +
                "let instance = new ClassB();\n" +
                "instance.method();\n" +
                "instance.method(11);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(10, 11), ApiRoot.intStorage.list);
    }

    @Test
    public void differentReturnTypesOverrideModifierTest() {
        String code =
                "class ClassA {\n" +
                "    void value() {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    int ⟦value⟧() => 1;\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.MethodOverrideReturnMismatch);
    }

    @Test
    public void missingOverrideModifierTest() {
        String code =
                "class ClassA {\n" +
                "    void value() {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    void ⟦value⟧() {}\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.OverrideMissing);
    }

    @Test
    public void missingVirtualModifierTest() {
        String code =
                "class ClassA {\n" +
                "    void value() {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    override void ⟦value⟧() {}\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.NonVirtualOverride);
    }

    @Test
    public void simpleOverrideTest() {
        String code =
                "class ClassA {\n" +
                "    virtual int value() => 1;\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    override int value() => 2;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(new ClassA().value());\n" +
                "intStorage.add(new ClassB().value());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
    }

    @Test
    public void baseCallOverrideTest() {
        String code =
                "class ClassA {\n" +
                "    virtual int value() => 1;\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    override int value() => base.value() + 1;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(new ClassA().value());\n" +
                "intStorage.add(new ClassB().value());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
    }

    @Test
    public void baseMethodInvalidArgumentsTest() {
        String code =
                "class ClassA {\n" +
                "    virtual void method(int x) {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    override void method(int x) {}\n" +
                "    void test() => base.method⟦(\"text\")⟧;\n" +
                "}\n";

        String candidates =
                "Candidates:\n" +
                "void method(int x)";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.MethodInvalidArguments,
                "method", candidates);
    }

    @Test
    public void baseMethodArgumentCountMismatchTest() {
        String code =
                "class ClassA {\n" +
                "    virtual void method(int x) {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    override void method(int x) {}\n" +
                "    void test() => base.⟦method⟧();\n" +
                "}\n";

        String candidates =
                "Candidates:\n" +
                "void method(int x)";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.NoOverloadedMethods,
                "method", 0, candidates);
    }

    @Test
    public void methodsShouldBeFinalByDefault() throws Exception {
        String code =
                "class Class {\n" +
                "    void method(){}\n" +
                "}\n" +
                "\n" +
                "objectStorage.add(new Class());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertEquals(1, ApiRoot.objectStorage.list.size());

        Method method = ApiRoot.objectStorage.list.get(0).getClass().getMethod("method");
        Assertions.assertTrue(Modifier.isFinal(method.getModifiers()));
    }

    @Test
    public void baseClassAssignTest() {
        String code =
                "class ClassA {\n" +
                "    virtual int value() => 1;\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    override int value() => 2;\n" +
                "}\n" +
                "\n" +
                "ClassA variable = new ClassA();\n" +
                "intStorage.add(variable.value());\n" +
                "variable = new ClassB();\n" +
                "intStorage.add(variable.value());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
    }

    @Test
    public void javaTypeInheritTest() {
        String code =
                "class MyList : Java<java.util.Vector> {\n" +
                "    void add(int value) => base.add(value);\n" +
                "    int get2(int index) => base.get(index) as int;\n" +
                "}\n" +
                "\n" +
                "let list = new MyList();\n" +
                "list.add(5);\n" +
                "list.add(6);\n" +
                "list.add(7);\n" +
                "for (int i = 0; i < list.size(); i++) {\n" +
                "    intStorage.add(list.get2(i));\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(5, 6, 7), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaMethodOverrideTest() throws Exception {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedOverrideBase> {\n" +
                "    protected override int transform(int value) => value + 1;\n" +
                "}\n" +
                "\n" +
                "let instance = new Class();\n" +
                "intStorage.add(instance.invoke(10));\n" +
                "objectStorage.add(instance);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(11), ApiRoot.intStorage.list);
        Method method = ApiRoot.objectStorage.list.get(0).getClass().getDeclaredMethod("transform", int.class);
        Assertions.assertTrue(Modifier.isProtected(method.getModifiers()));
    }

    @Test
    public void protectedScriptConstructorTest() throws Exception {
        String code =
                "class Base {\n" +
                "    protected int value;\n" +
                "    protected constructor(int value) {\n" +
                "        this.value = value;\n" +
                "    }\n" +
                "}\n" +
                "class Child : Base {\n" +
                "    public constructor() : base(17) {}\n" +
                "    public int getValue() => value;\n" +
                "}\n" +
                "\n" +
                "let instance = new Child();\n" +
                "intStorage.add(instance.getValue());\n" +
                "objectStorage.add(instance);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(17), ApiRoot.intStorage.list);
        Class<?> baseClass = ApiRoot.objectStorage.list.get(0).getClass().getSuperclass();
        Assertions.assertTrue(Modifier.isProtected(baseClass.getDeclaredConstructor(int.class).getModifiers()));
    }

    @Test
    public void protectedScriptMembersOnSubclassReceiverTest() {
        String code =
                "class Base {\n" +
                "    protected int value;\n" +
                "\n" +
                "    protected void setValue(int value) {\n" +
                "        this.value = value;\n" +
                "    }\n" +
                "}\n" +
                "class Child : Base {\n" +
                "    public void copyFrom(Child other) {\n" +
                "        other.setValue(43);\n" +
                "        value = other.value;\n" +
                "    }\n" +
                "\n" +
                "    public int getValue() => value;\n" +
                "}\n" +
                "\n" +
                "let instance = new Child();\n" +
                "instance.copyFrom(new Child());\n" +
                "intStorage.add(instance.getValue());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(43), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedScriptMemberOnBaseReceiverTest() {
        String code =
                "class Base {\n" +
                "    protected int value;\n" +
                "}\n" +
                "class Child : Base {\n" +
                "    public int read(Base other) => other.⟦value⟧;\n" +
                "}\n";

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
        String code =
                "class Base {\n" +
                "    protected void method() {}\n" +
                "}\n" +
                "class First : Base {\n" +
                "    public void call(Second other) => other.⟦method⟧();\n" +
                "}\n" +
                "class Second : Base {}\n";

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
        String code =
                "class Base {\n" +
                "    protected virtual int getValue() => 47;\n" +
                "}\n" +
                "class Child : Base {\n" +
                "    public override int getValue() => base.getValue() + 1;\n" +
                "}\n" +
                "\n" +
                "let instance = new Child();\n" +
                "intStorage.add(instance.getValue());\n" +
                "objectStorage.add(instance);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(48), ApiRoot.intStorage.list);
        Method method = ApiRoot.objectStorage.list.get(0).getClass().getDeclaredMethod("getValue");
        Assertions.assertTrue(Modifier.isPublic(method.getModifiers()));
    }

    @Test
    public void privateBaseConstructorCannotBeCalledExplicitlyTest() {
        String code =
                "class Base {\n" +
                "    private constructor(int value) {}\n" +
                "}\n" +
                "class Child : Base {\n" +
                "    constructor() : ⟦base(1)⟧ {}\n" +
                "}\n";

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.NoConstructors,
                "Base");
    }

    @Test
    public void cannotReducePublicMethodVisibilityTest() {
        String code =
                "class Base {\n" +
                "    public virtual void method() {}\n" +
                "}\n" +
                "class Child : Base {\n" +
                "    ⟦protected⟧ override void method() {}\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.CannotReduceMethodVisibility);
    }

    @Test
    public void cannotReduceProtectedMethodVisibilityTest() {
        String code =
                "class Base {\n" +
                "    protected virtual void method() {}\n" +
                "}\n" +
                "class Child : Base {\n" +
                "    ⟦private⟧ override void method() {}\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.CannotReduceMethodVisibility);
    }

    @Test
    public void privateBaseMembersCanBeRedeclaredTest() {
        String code =
                "class Base {\n" +
                "    private int value;\n" +
                "    private int method() => 1;\n" +
                "    public int getBaseValue() => value + method();\n" +
                "}\n" +
                "class Child : Base {\n" +
                "    public int value;\n" +
                "    public int method() => 2;\n" +
                "}\n" +
                "\n" +
                "let instance = new Child();\n" +
                "instance.value = 3;\n" +
                "intStorage.add(instance.getBaseValue());\n" +
                "intStorage.add(instance.method());\n" +
                "intStorage.add(instance.value);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2, 3), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedMethodDoesNotImplementPublicInterfaceMethodTest() {
        String code =
                "class Base {\n" +
                "    protected virtual void run() {}\n" +
                "}\n" +
                "class ⟦Child⟧ : Base, Java<java.lang.Runnable> {}\n";

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MissingInheritedMethodImplementation,
                "run");
    }

    @Test
    public void protectedJavaMethodBaseCallTest1() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedMethodBase> {\n" +
                "    constructor() {\n" +
                "        base.add(123);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "new Class();\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaMethodBaseCallTest2() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedMethodBase> {\n" +
                "    constructor() {\n" +
                "        this.add(123);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "new Class();\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaMethodBaseCallTest3() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedMethodBase> {\n" +
                "    constructor() {\n" +
                "        add(123);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "new Class();\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaMethodOnCapturedThisInLambdaTest() {
        String code =
                "typealias Run = Java<com.zergatul.scripting.tests.compiler.helpers.Run>;\n" +
                "\n" +
                "class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedMethodBase> {\n" +
                "    void execute() {\n" +
                "        let run = new Run();\n" +
                "        let self = this;\n" +
                "        run.once(() => {\n" +
                "            self.value = 100;\n" +
                "            self.value += 23;\n" +
                "            self.add(self.value);\n" +
                "        });\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "new Class().execute();\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedScriptMembersOnSubclassReceiverInLambdaTest() {
        String code =
                "typealias Run = Java<com.zergatul.scripting.tests.compiler.helpers.Run>;\n" +
                "\n" +
                "class Base {\n" +
                "    protected int value;\n" +
                "\n" +
                "    protected void setValue(int value) {\n" +
                "        this.value = value;\n" +
                "    }\n" +
                "}\n" +
                "class Child : Base {\n" +
                "    public void copyFrom(Child other) {\n" +
                "        let self = this;\n" +
                "        new Run().once(() => {\n" +
                "            other.setValue(61);\n" +
                "            self.value = other.value;\n" +
                "        });\n" +
                "    }\n" +
                "\n" +
                "    public int getValue() => value;\n" +
                "}\n" +
                "\n" +
                "let instance = new Child();\n" +
                "instance.copyFrom(new Child());\n" +
                "intStorage.add(instance.getValue());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(61), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedScriptMemberOnBaseReceiverInLambdaTest() {
        String code =
                "typealias Run = Java<com.zergatul.scripting.tests.compiler.helpers.Run>;\n" +
                "\n" +
                "class Base {\n" +
                "    protected int value;\n" +
                "}\n" +
                "class Child : Base {\n" +
                "    public void read(Base other) {\n" +
                "        new Run().once(() => intStorage.add(other.⟦value⟧));\n" +
                "    }\n" +
                "}\n";

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
        String code =
                "class Class : Java<java.util.Vector> {\n" +
                "    constructor() {\n" +
                "        base.add(1);\n" +
                "        base.add(2);\n" +
                "        base.removeRange(0, 1);\n" +
                "        intStorage.add(base.size());\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "new Class();\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaMethodOnSubclassReceiverTest() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedMethodBase> {\n" +
                "    void call(Class other) {\n" +
                "        other.add(321);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "new Class().call(new Class());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(321), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaMethodOnBaseReceiverTest() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedMethodBase> {\n" +
                "    void call(Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedMethodBase> other) {\n" +
                "        other.⟦add⟧(321);\n" +
                "    }\n" +
                "}\n";

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
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedFieldBase> {\n" +
                "    constructor() {\n" +
                "        value = 10;\n" +
                "        this.value += 5;\n" +
                "        value++;\n" +
                "        intStorage.add(this.value);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "new Class();\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(16), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaFieldOnSubclassReceiverTest() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedFieldBase> {\n" +
                "    void call(Class other) {\n" +
                "        other.value = 321;\n" +
                "        intStorage.add(other.value);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "new Class().call(new Class());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(321), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaFieldOnBaseReceiverTest() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedFieldBase> {\n" +
                "    void call(Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedFieldBase> other) {\n" +
                "        intStorage.add(other.⟦value⟧);\n" +
                "    }\n" +
                "}\n";

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
        String code =
                "class Class : Java<java.io.ByteArrayInputStream> {\n" +
                "    constructor() : base(new int8[0]) {\n" +
                "        this.pos = 7;\n" +
                "        intStorage.add(this.pos);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "new Class();\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(7), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedStaticJavaFieldDirectAccessTest() {
        String code =
                "typealias Base = Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedFieldBase>;\n" +
                "\n" +
                "class Class : Base {\n" +
                "    constructor() {\n" +
                "        Base.staticValue = 10;\n" +
                "        Base.staticValue++;\n" +
                "        Base.staticValue += 5;\n" +
                "        intStorage.add(Base.staticValue);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "new Class();\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(16), ApiRoot.intStorage.list);
    }

    @Test
    public void cannotInstantiateAbstractClassTest() {
        String code =
                "let list = ⟦new Java<java.util.AbstractList>()⟧;\n";

        comparator.assertDiagnostics(ApiRoot.class, code,
                new MarkedDiagnostic("⟦⟧", BinderErrors.CannotInstantiateAbstractClass),
                new MarkedDiagnostic("⟦⟧", BinderErrors.NoOverloadedConstructors, "Java<java.util.AbstractList>", 0, "No candidates"));
    }

    @Test
    public void cannotInstantiateInterfaceTest() {
        String code =
                "let list = ⟦new Java<java.util.List>()⟧;\n";

        comparator.assertDiagnostics(ApiRoot.class, code,
                new MarkedDiagnostic("⟦⟧", BinderErrors.CannotInstantiateAbstractClass),
                new MarkedDiagnostic("⟦⟧", BinderErrors.NoOverloadedConstructors, "Java<java.util.List>", 0, "No candidates"));
    }

    @Test
    public void baseInExtensionTest() {
        String code =
                "extension(int) {\n" +
                "    void method() => ⟦base⟧.toString();\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseInvalidContext);
    }

    @Test
    public void cannotUseBaseAsValueTest() {
        String code =
                "class Class {\n" +
                "    void method() {\n" +
                "        let x = ⟦base⟧;\n" +
                "    }\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseInvalidUse);
    }

    @Test
    public void overrideJavaClassMethodTest() {
        String code =
                "class MyList : Java<java.util.Vector> {\n" +
                "    override int size() => 12;\n" +
                "}\n" +
                "\n" +
                "let list = new MyList();\n" +
                "intStorage.add(list.size());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(12), ApiRoot.intStorage.list);
    }

    @Test
    public void cannotOverrideFinalMethodTest() {
        String code =
                "class Class {\n" +
                "    override void ⟦notify⟧() {\n" +
                "        base.notify();\n" +
                "    }\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.NonVirtualOverride);
    }

    @Test
    public void javaInterfaceImplementationMissingMethodTest() {
        String code =
                "class ⟦Class⟧ : Java<java.lang.Runnable> {}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.MissingInheritedMethodImplementation, "run");
    }

    @Test
    public void javaInterfaceImplementationTest() {
        String code =
                "class Class : Java<java.lang.Runnable> {\n" +
                "    override void run() {\n" +
                "        intStorage.add(123);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "let instance = new Class();\n" +
                "Java<java.lang.Runnable> runnable = instance;\n" +
                "runnable.run();\n" +
                "objectStorage.add(instance);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(123), ApiRoot.intStorage.list);
        Assertions.assertTrue(ApiRoot.objectStorage.list.get(0) instanceof Runnable);
    }

    @Test
    public void syntheticJavaMethodImplementsInterfaceMethodTest() throws Exception {
        Class<?> baseClass = SyntheticMethodBaseHolder.TYPE;
        Method method = baseClass.getDeclaredMethod("value");

        Assertions.assertTrue(method.isSynthetic());
        Assertions.assertFalse(method.isBridge());
        Assertions.assertFalse(Modifier.isAbstract(method.getModifiers()));
        Assertions.assertEquals(SyntheticMethodContract.class, baseClass.getInterfaces()[0]);

        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$SyntheticMethodBase> {}\n" +
                "\n" +
                "objectStorage.add(new Class());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Object object = ApiRoot.objectStorage.list.get(0);
        Assertions.assertTrue(object instanceof SyntheticMethodContract);
        Assertions.assertEquals(123, ((SyntheticMethodContract) object).value());
    }

    @Test
    public void bridgeJavaMethodImplementsExplicitRawInterfaceMethodTest() {
        Method bridge = Arrays.stream(GenericValueBase.class.getDeclaredMethods())
                .filter(Method::isBridge)
                .findFirst()
                .orElseThrow(NoSuchElementException::new);

        Assertions.assertTrue(bridge.isSynthetic());
        Assertions.assertFalse(Modifier.isAbstract(bridge.getModifiers()));
        Assertions.assertEquals(Object.class, bridge.getReturnType());

        String code =
                "class Class :\n" +
                "    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$GenericValueBase>,\n" +
                "    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$GenericValue> {}\n" +
                "\n" +
                "objectStorage.add(new Class());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Object object = ApiRoot.objectStorage.list.get(0);
        Assertions.assertTrue(object instanceof GenericValue);
        Assertions.assertEquals("bridge", ((GenericValue<?>) object).value());
    }

    @Test
    public void javaDefaultInterfaceMethodDoesNotRequireImplementationTest() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$DefaultMethodInterface> {}\n" +
                "\n" +
                "objectStorage.add(new Class());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Object object = ApiRoot.objectStorage.list.get(0);
        Assertions.assertTrue(object instanceof DefaultMethodInterface);
        Assertions.assertEquals(123, ((DefaultMethodInterface) object).value());
    }

    @Test
    public void moreSpecificDefaultInterfaceMethodImplementsAbstractBaseContractTest() {
        String code =
                "class Class :\n" +
                "    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$AbstractInterfaceBase>,\n" +
                "    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$MoreSpecificDefaultInterface> {}\n" +
                "\n" +
                "objectStorage.add(new Class());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Object object = ApiRoot.objectStorage.list.get(0);
        Assertions.assertTrue(object instanceof AbstractMethodInterface);
        Assertions.assertEquals(123, ((AbstractMethodInterface) object).value());
    }

    @Test
    public void abstractClassMethodTakesPrecedenceOverDefaultInterfaceMethodTest() {
        String code =
                "class ⟦Class⟧ :\n" +
                "    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$AbstractMethodBase>,\n" +
                "    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$MoreSpecificDefaultInterface> {}\n";

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MissingInheritedMethodImplementation,
                "value");
    }

    @Test
    public void conflictingDefaultInterfaceMethodsRequireImplementationTest() {
        String code =
                "class ⟦Class⟧ :\n" +
                "    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$FirstDefaultMethodInterface>,\n" +
                "    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$SecondDefaultMethodInterface> {}\n";

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
        String code =
                "class ⟦Class⟧ :\n" +
                "    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$AbstractMethodInterface>,\n" +
                "    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$DefaultMethodInterface> {}\n";

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MissingInheritedMethodImplementation,
                "value");
    }

    @Test
    public void abstractSubinterfaceMethodSuppressesParentDefaultMethodTest() {
        String code =
                "class ⟦Class⟧ :\n" +
                "    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$DefaultMethodInterface>,\n" +
                "    Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$AbstractRedeclaringInterface> {}\n";

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MissingInheritedMethodImplementation,
                "value");
    }

    @Test
    public void javaMultipleInterfaceImplementationTest() {
        String code =
                "class Class : Java<java.lang.Runnable>, Java<java.lang.AutoCloseable> {\n" +
                "    override void run() {\n" +
                "        intStorage.add(1);\n" +
                "    }\n" +
                "    override void close() {\n" +
                "        intStorage.add(2);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "let instance = new Class();\n" +
                "instance.run();\n" +
                "instance.close();\n" +
                "objectStorage.add(instance);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
        Object object = ApiRoot.objectStorage.list.get(0);
        Assertions.assertTrue(object instanceof Runnable);
        Assertions.assertTrue(object instanceof AutoCloseable);
    }

    @Test
    public void javaClassAndInterfaceImplementationTest() {
        String code =
                "class Class : Java<java.util.ArrayList>, Java<java.lang.Runnable> {\n" +
                "    override void run() {\n" +
                "        intStorage.add(this.size());\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "let instance = new Class();\n" +
                "instance.add(10);\n" +
                "instance.add(20);\n" +
                "instance.run();\n" +
                "objectStorage.add(instance);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(2), ApiRoot.intStorage.list);
        Object object = ApiRoot.objectStorage.list.get(0);
        Assertions.assertTrue(object instanceof java.util.ArrayList);
        Assertions.assertTrue(object instanceof Runnable);
        Assertions.assertIterableEquals(Lists.of(10, 20), (List<?>) object);
    }

    @Test
    public void multipleJavaBaseClassesTest() {
        String code =
                "class Class : Java<java.util.ArrayList>, ⟦Java<java.util.Vector>⟧ {}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.MultipleBaseClasses);
    }

    @Test
    public void javaAbstractClassMissingMethodTest() {
        String code =
                "class ⟦Class⟧ : AbstractBase {}\n";

        comparator.assertDiagnostics(
                new ComparatorCompilationParameters.Builder().api(ApiRoot.class).customType(AbstractBase.class).build(),
                code, "⟦⟧",
                BinderErrors.MissingInheritedMethodImplementation,
                "value");
    }

    @Test
    public void javaAbstractClassImplementationRequiresOverrideTest() {
        String code =
                "class Class : AbstractBase {\n" +
                "    int ⟦value⟧() => 1;\n" +
                "}\n";

        comparator.assertDiagnostics(
                new ComparatorCompilationParameters.Builder().api(ApiRoot.class).customType(AbstractBase.class).build(),
                code, "⟦⟧",
                BinderErrors.OverrideMissing);
    }

    @Test
    public void abstractMethodNotSupportedTest() {
        String code =
                "class Class {\n" +
                "    abstract void ⟦run⟧() {}\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.AbstractMethodNotSupported);
    }

    @Test
    public void constructorInitializerBaseSimpleTest() {
        String code =
                "class ClassA {\n" +
                "    int x;\n" +
                "    constructor(int value) {\n" +
                "        x = value;\n" +
                "    }\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    constructor(int value1, int value2) : base(value1 + value2) {}\n" +
                "}\n" +
                "\n" +
                "let instance = new ClassB(10, 4);\n" +
                "intStorage.add(instance.x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(14), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaConstructorExplicitBaseCallTest() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedConstructorBase> {\n" +
                "    constructor(string value) : base(value) {}\n" +
                "}\n" +
                "\n" +
                "let instance = new Class(\"aa\");\n" +
                "stringStorage.add(instance.getValue());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("aa"), ApiRoot.stringStorage.list);
    }

    @Test
    public void protectedJavaConstructorImplicitBaseCallTest() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedConstructorBase> {\n" +
                "    constructor() {}\n" +
                "}\n" +
                "\n" +
                "let instance = new Class();\n" +
                "stringStorage.add(instance.getValue());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("default"), ApiRoot.stringStorage.list);
    }

    @Test
    public void protectedJavaConstructorSynthesizedBaseCallTest() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedConstructorBase> {}\n" +
                "\n" +
                "let instance = new Class();\n" +
                "stringStorage.add(instance.getValue());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("default"), ApiRoot.stringStorage.list);
    }

    @Test
    public void protectedJavaConstructorObjectCreationTest() {
        String code =
                "let instance = ⟦new Java<com.zergatul.scripting.tests.compiler.ClassInheritanceTests$ProtectedConstructorBase>(\"aa\")⟧;\n";

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
        String code =
                "class ClassA {\n" +
                "    constructor(string value) {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    constructor() : base(\"default\") {}\n" +
                "    constructor(string value) : ⟦base()⟧ {}\n" +
                "}\n";

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.NoOverloadedConstructors,
                "ClassA",
                0,
                "Candidates:\n" +
                "constructor ClassA(string value)");
    }

    @Test
    public void constructorInitializerBaseInvalidArgumentsTest() {
        String code =
                "class ClassA {\n" +
                "    constructor(int value) {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    constructor() : base⟦(\"text\")⟧ {}\n" +
                "}\n";

        String candidates =
                "Candidates:\n" +
                "constructor ClassA(int value)";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.ConstructorInvalidArguments,
                "ClassA", candidates);
    }

    @Test
    public void constructorInitializerThisSimpleTest() {
        String code =
                "class ClassA {\n" +
                "    int x;\n" +
                "    constructor(int value) {\n" +
                "        x = value;\n" +
                "    }\n" +
                "    constructor(int value1, int value2) : this(value1 + value2) {}\n" +
                "}\n" +
                "\n" +
                "let instance = new ClassA(10, 4);\n" +
                "intStorage.add(instance.x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(14), ApiRoot.intStorage.list);
    }

    @Test
    public void constructorInitializerThisInvalidArgumentsTest() {
        String code =
                "class ClassA {\n" +
                "    constructor(int value) {}\n" +
                "    constructor() : this⟦(\"text\")⟧ {}\n" +
                "}\n";

        String candidates =
                "Candidates:\n" +
                "constructor ClassA()\n" +
                "constructor ClassA(int value)";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.ConstructorInvalidArguments,
                "ClassA", candidates);
    }

    @Test
    public void noDefaultConstructorTest1() {
        String code =
                "class ClassA {\n" +
                "    constructor(int x) {}\n" +
                "}\n" +
                "class ⟦ClassB⟧ : ClassA {}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseClassNoParameterlessConstructor);
    }

    @Test
    public void noDefaultConstructorTest2() {
        String code =
                "class ClassA {\n" +
                "    constructor(int x) {}\n" +
                "}\n" +
                "class ClassB : ClassA {\n" +
                "    ⟦constructor⟧(int x) {}\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseClassNoParameterlessConstructor);
    }

    @Test
    public void selfInheritTest() {
        String code =
                "class ClassA : ⟦ClassA⟧ {}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.ClassCircularInheritance);
    }

    @Test
    public void inheritanceLoopTest() {
        String code =
                "class ClassA : ClassB {}\n" +
                "class ClassB : ⟦ClassA⟧ {}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.ClassCircularInheritance);
    }

    @Test
    public void inheritVoidTest() {
        String code =
                "class Class : ⟦void⟧ ⟪{⟫}\n";

        comparator.assertDiagnostics(ApiRoot.class, code,
                new MarkedDiagnostic("⟦⟧", ParserErrors.TypeExpected, "void"),
                new MarkedDiagnostic("⟦⟧", ParserErrors.OpenCurlyBracketExpected, "void"),
                new MarkedDiagnostic("⟪⟫", ParserErrors.IdentifierExpected, "{"));
    }

    @Test
    public void inheritIntTest() {
        String code =
                "class ⟦Class⟧ : int {}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.BaseClassNoParameterlessConstructor);
    }

    @Test
    public void inheritFuncTest() {
        String code =
                "class ⟦Class⟧ : fn<int => int> {}\n";

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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public interface FirstDefaultMethodInterface {
        default int value() {
            return 1;
        }
    }

    @SuppressWarnings("unused")
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
                V1_8,
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
            byte[] bytecode = writer.toByteArray();
            Method defineClass = ClassLoader.class.getDeclaredMethod(
                    "defineClass", String.class, byte[].class, int.class, int.class);
            defineClass.setAccessible(true);
            return (Class<?>) defineClass.invoke(
                    ClassInheritanceTests.class.getClassLoader(), className, bytecode, 0, bytecode.length);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}