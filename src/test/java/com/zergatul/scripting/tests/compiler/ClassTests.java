package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.AsyncRunnable;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.parser.ParserErrors;
import com.zergatul.scripting.tests.compiler.helpers.FutureHelper;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.ObjectStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.*;

public class ClassTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.objectStorage = new ObjectStorage();
        ApiRoot.futures = new FutureHelper();
    }

    @Test
    public void basicTest() {
        String code =
                "class Class{}\n" +
                "\n" +
                "Class x = new Class();\n" +
                "stringStorage.add(#typeof(x).name);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("Class"));
    }

    @Test
    public void functionTest() {
        String code =
                "Class func(Class c) { return c; }\n" +
                "class Class {}\n" +
                "\n" +
                "stringStorage.add(#typeof(func(new Class())).name);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("Class"));
    }

    @Test
    public void staticVariableTest() {
        String code =
                "static Class cc = new Class();\n" +
                "class Class {}\n" +
                "\n" +
                "stringStorage.add(#typeof(cc).name);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("Class"));
    }

    @Test
    public void classRedefineTest() {
        String code =
                "class Class {}\n" +
                "class Class {}\n" +
                "\n" +
                "Class c = new Class();\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.SymbolAlreadyDeclared, new SingleLineTextRange(2, 7, 21, 5), "Class")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void fieldTest() {
        String code =
                "class Class {\n" +
                "    int x;\n" +
                "}\n" +
                "\n" +
                "Class c = new Class();\n" +
                "c.x = 123;\n" +
                "c.x++;\n" +
                "c.x += 2;\n" +
                "intStorage.add(c.x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(126));
    }

    @Test
    public void memberVisibilityTest() throws Exception {
        String code =
                "class Class {\n" +
                "    int defaultField;\n" +
                "    public int publicField;\n" +
                "    protected int protectedField;\n" +
                "    private int privateField;\n" +
                "\n" +
                "    public constructor() : this(7) {}\n" +
                "    private constructor(int value) {\n" +
                "        privateField = value;\n" +
                "    }\n" +
                "\n" +
                "    int defaultMethod() => defaultField;\n" +
                "    public int publicMethod() => publicField;\n" +
                "    protected int protectedMethod() => protectedField;\n" +
                "    private int privateMethod() => privateField;\n" +
                "\n" +
                "    public int callPrivate(Class other) => other.privateField + other.privateMethod();\n" +
                "}\n" +
                "\n" +
                "let instance = new Class();\n" +
                "objectStorage.add(instance);\n" +
                "intStorage.add(instance.callPrivate(new Class()));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(14), ApiRoot.intStorage.list);
        Class<?> clazz = ApiRoot.objectStorage.list.get(0).getClass();

        assertVisibility(clazz.getDeclaredField("defaultField"), Modifier.PUBLIC);
        assertVisibility(clazz.getDeclaredField("publicField"), Modifier.PUBLIC);
        assertVisibility(clazz.getDeclaredField("protectedField"), Modifier.PROTECTED);
        assertVisibility(clazz.getDeclaredField("privateField"), Modifier.PRIVATE);

        assertVisibility(clazz.getDeclaredMethod("defaultMethod"), Modifier.PUBLIC);
        assertVisibility(clazz.getDeclaredMethod("publicMethod"), Modifier.PUBLIC);
        assertVisibility(clazz.getDeclaredMethod("protectedMethod"), Modifier.PROTECTED);
        assertVisibility(clazz.getDeclaredMethod("privateMethod"), Modifier.PRIVATE);

        assertVisibility(clazz.getDeclaredConstructor(), Modifier.PUBLIC);
        assertVisibility(clazz.getDeclaredConstructor(int.class), Modifier.PRIVATE);
    }

    @Test
    public void privateMembersOnCapturedThisInLambdaTest() {
        String code =
                "typealias Run = Java<com.zergatul.scripting.tests.compiler.helpers.Run>;\n" +
                "\n" +
                "class Class {\n" +
                "    private int field;\n" +
                "\n" +
                "    private void add(int value) {\n" +
                "        field += value;\n" +
                "    }\n" +
                "\n" +
                "    public void execute() {\n" +
                "        let self = this;\n" +
                "        new Run().once(() => {\n" +
                "            self.field = 100;\n" +
                "            self.add(23);\n" +
                "            intStorage.add(self.field);\n" +
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
    public void privateConstructorFromLambdaTest() {
        String code =
                "typealias Run = Java<com.zergatul.scripting.tests.compiler.helpers.Run>;\n" +
                "\n" +
                "class Class {\n" +
                "    private int value;\n" +
                "\n" +
                "    public constructor() : this(0) {}\n" +
                "\n" +
                "    private constructor(int value) {\n" +
                "        this.value = value;\n" +
                "    }\n" +
                "\n" +
                "    private int getValue() => value;\n" +
                "\n" +
                "    public void execute() {\n" +
                "        new Run().once(() => {\n" +
                "            let instance = new Class(53);\n" +
                "            intStorage.add(instance.getValue());\n" +
                "        });\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "new Class().execute();\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(53), ApiRoot.intStorage.list);
    }

    @Test
    public void privateMembersFromNestedLambdaTest() {
        String code =
                "typealias Run = Java<com.zergatul.scripting.tests.compiler.helpers.Run>;\n" +
                "\n" +
                "class Class {\n" +
                "    private int value;\n" +
                "\n" +
                "    private void add(int delta) {\n" +
                "        value += delta;\n" +
                "    }\n" +
                "\n" +
                "    public void execute() {\n" +
                "        let self = this;\n" +
                "        int outer = 7;\n" +
                "        new Run().once(() => {\n" +
                "            int inner = 11;\n" +
                "            new Run().once(() => {\n" +
                "                self.add(outer + inner);\n" +
                "                intStorage.add(self.value);\n" +
                "            });\n" +
                "        });\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "new Class().execute();\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(18), ApiRoot.intStorage.list);
    }

    @Test
    public void privateMembersFromDeferredGenericLambdaTest() {
        String code =
                "typealias Run = Java<com.zergatul.scripting.tests.compiler.helpers.Run>;\n" +
                "\n" +
                "class Class {\n" +
                "    private int value;\n" +
                "\n" +
                "    public Run createHandler() {\n" +
                "        let run = new Run();\n" +
                "        let self = this;\n" +
                "        run.onInteger(delta => self.value += delta);\n" +
                "        return run;\n" +
                "    }\n" +
                "\n" +
                "    public int getValue() => value;\n" +
                "}\n" +
                "\n" +
                "let instance = new Class();\n" +
                "let run = instance.createHandler();\n" +
                "run.triggerInteger(59);\n" +
                "intStorage.add(instance.getValue());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(59), ApiRoot.intStorage.list);
    }

    @Test
    public void privateMemberCannotBeAccessedOutsideClassTest() {
        String code =
                "class Class {\n" +
                "    private int value;\n" +
                "}\n" +
                "\n" +
                "new Class().⟦value⟧ = 1;\n";

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MemberDoesNotExist,
                "Class",
                "value");
    }

    @Test
    public void protectedMemberCannotBeAccessedOutsideClassTest() {
        String code =
                "class Class {\n" +
                "    protected void method() {}\n" +
                "}\n" +
                "\n" +
                "new Class().⟦method⟧();\n";

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.MemberDoesNotExist,
                "Class",
                "method");
    }

    @Test
    public void privateConstructorCannotBeCalledOutsideClassTest() {
        String code =
                "class Class {\n" +
                "    private constructor() {}\n" +
                "}\n" +
                "\n" +
                "⟦new Class()⟧;\n";

        comparator.assertDiagnostics(
                ApiRoot.class,
                code,
                "⟦⟧",
                BinderErrors.NoOverloadedConstructors,
                "Class",
                0,
                "No candidates");
    }

    @Test
    public void visibilityModifierOnFunctionTest() {
        String code =
                "⟦private⟧ void function() {}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.VisibilityModifierNotAllowed);
    }

    @Test
    public void visibilityModifierOnExtensionMethodTest() {
        String code =
                "extension(int) {\n" +
                "    ⟦protected⟧ void method() {}\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.VisibilityModifierNotAllowed);
    }

    @Test
    public void privateVirtualMethodTest() {
        String code =
                "class Class {\n" +
                "    ⟦private⟧ virtual void method() {}\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", BinderErrors.PrivateMethodCannotBeVirtual);
    }

    @Test
    public void conflictingVisibilityModifiersTest() {
        String code =
                "class Class {\n" +
                "    public ⟦private⟧ int value;\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", ParserErrors.ConflictingVisibilityModifiers);
    }

    @Test
    public void invalidConstructorModifierTest() {
        String code =
                "class Class {\n" +
                "    ⟦async⟧ constructor() {}\n" +
                "}\n";

        comparator.assertDiagnostics(ApiRoot.class, code, "⟦⟧", ParserErrors.ClassMemberModifiersNotAllowed);
    }

    @Test
    public void selfReferenceTest() {
        String code =
                "class ListItem {\n" +
                "    ListItem next;\n" +
                "}\n" +
                "\n" +
                "int len(ListItem item) {\n" +
                "    if (item.next is ListItem) {\n" +
                "        return len(item.next) + 1;\n" +
                "    } else {\n" +
                "        return 1;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "let items = new ListItem[10];\n" +
                "for (let i = 0; i < items.length; i++) {\n" +
                "    items[i] = new ListItem();\n" +
                "}\n" +
                "\n" +
                "for (let i = 0; i < items.length - 1; i++) {\n" +
                "    items[i].next = items[i + 1];\n" +
                "}\n" +
                "\n" +
                "intStorage.add(len(items[0]));\n" +
                "intStorage.add(len(items[1]));\n" +
                "intStorage.add(len(items[5]));\n" +
                "intStorage.add(len(items[9]));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 9, 5, 1));
    }

    @Test
    public void fieldRedefineTest() {
        String code =
                "class Item {\n" +
                "    int val;\n" +
                "    int val;\n" +
                "}\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.MemberAlreadyDeclared, new SingleLineTextRange(3, 9, 34, 3), "val")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void castToObjectTest() {
        String code =
                "class Class {}\n" +
                "\n" +
                "objectStorage.add(new Class());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertEquals(ApiRoot.objectStorage.list.size(), 1);
        Assertions.assertEquals(ApiRoot.objectStorage.list.get(0).getClass().getSimpleName(), "Class");
    }

    @Test
    public void constructorTest1() {
        String code =
                "class Class {\n" +
                "    constructor(int x) {\n" +
                "        this.x = x;\n" +
                "    }\n" +
                "    int x;\n" +
                "}\n" +
                "\n" +
                "let c = new Class(10);\n" +
                "intStorage.add(c.x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10));
    }

    @Test
    public void constructorTest2() {
        String code =
                "class Class {\n" +
                "    constructor(boolean b, int x) {\n" +
                "        if (b) {\n" +
                "            this.x = x + x;\n" +
                "        } else {\n" +
                "            this.x = x * x;\n" +
                "        }\n" +
                "    }\n" +
                "    int x;\n" +
                "}\n" +
                "\n" +
                "let c1 = new Class(false, 10);\n" +
                "let c2 = new Class(true, 20);\n" +
                "intStorage.add(c1.x);\n" +
                "intStorage.add(c2.x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(100, 40));
    }

    @Test
    public void constructorTest3() {
        String code =
                "int sum(TreeEntry entry) {\n" +
                "    int total = entry.value;\n" +
                "    if (entry.left is TreeEntry) {\n" +
                "        total += entry.left.value;\n" +
                "    }\n" +
                "    if (entry.right is TreeEntry) {\n" +
                "        total += entry.right.value;\n" +
                "    }\n" +
                "    return total;\n" +
                "}\n" +
                "\n" +
                "class TreeEntry {\n" +
                "    constructor(int value) {\n" +
                "        this.value = value;\n" +
                "    }\n" +
                "    constructor(TreeEntry left, TreeEntry right, int value) {\n" +
                "        this.left = left;\n" +
                "        this.right = right;\n" +
                "        this.value = value;\n" +
                "    }\n" +
                "    TreeEntry left;\n" +
                "    TreeEntry right;\n" +
                "    int value;\n" +
                "}\n" +
                "\n" +
                "let tree = new TreeEntry(\n" +
                "    new TreeEntry(10),\n" +
                "    new TreeEntry(20),\n" +
                "    50);\n" +
                "intStorage.add(sum(tree));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(80));
    }

    @Test
    public void constructorTest4() {
        String code =
                "class Fib {\n" +
                "    constructor(int value) {\n" +
                "        if (value <= 2) {\n" +
                "            this.value = 1;\n" +
                "        } else {\n" +
                "            this.value = new Fib(value - 1).value + new Fib(value - 2).value;\n" +
                "        }\n" +
                "    }\n" +
                "    int value;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(new Fib(8).value);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(21));
    }

    @Test
    public void constructorRedefineTest() {
        String code =
                "class Test {\n" +
                "    constructor(Test s1, Test s2) {}\n" +
                "    constructor(Test t1, Test t2) {}\n" +
                "}\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.ConstructorAlreadyDeclared, new SingleLineTextRange(3, 5, 54, 11))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void methodTest1() {
        String code =
                "class Class {\n" +
                "    void add(int x) {\n" +
                "        intStorage.add(x);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "let c = new Class();\n" +
                "c.add(5);\n" +
                "c.add(3);\n" +
                "c.add(1);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(5, 3, 1));
    }

    @Test
    public void methodTest2() {
        String code =
                "class Class {\n" +
                "    void add(int x) {\n" +
                "        intStorage.add(x);\n" +
                "    }\n" +
                "    void add(int x, int y) {\n" +
                "        intStorage.add(x + y);\n" +
                "    }\n" +
                "    void add(int x, int y, int z) {\n" +
                "        intStorage.add(x + y + z);\n" +
                "    }\n" +
                "    void add(string s) {\n" +
                "        stringStorage.add(s);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "let c = new Class();\n" +
                "c.add(5);\n" +
                "c.add(3, 5);\n" +
                "c.add(1, 3, 5);\n" +
                "c.add(\"qq\");\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(5, 8, 9));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("qq"));
    }

    @Test
    public void methodTest3() {
        String code =
                "class Class {\n" +
                "    int inc(int x) {\n" +
                "        return x + 1;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "intStorage.add(new Class().inc(5));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(6));
    }

    @Test
    public void methodTest4() {
        String code =
                "class Class {\n" +
                "    int factorial1(int x) {\n" +
                "        return x > 1 ? x * this.factorial2(x - 1) : 1;\n" +
                "    }\n" +
                "    int factorial2(int x) {\n" +
                "        return x > 1 ? x * this.factorial3(x - 1) : 1;\n" +
                "    }\n" +
                "    int factorial3(int x) {\n" +
                "        return x > 1 ? x * this.factorial1(x - 1) : 1;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "intStorage.add(new Class().factorial1(10));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(3628800));
    }

    @Test
    public void methodTest5() {
        String code =
                "class Calculator {\n" +
                "    int factorial(int x) {\n" +
                "        let c1 = new Class1();\n" +
                "        let c2 = new Class2();\n" +
                "        if (x % 2 == 0) {\n" +
                "            return c1.factorial(x, c2);\n" +
                "        } else {\n" +
                "            return c2.factorial(x, c1);\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "class Class1 {\n" +
                "    int factorial(int x, Class2 c) {\n" +
                "        return x > 1 ? x * c.factorial(x - 1, this) : 1;\n" +
                "    }\n" +
                "}\n" +
                "class Class2 {\n" +
                "    int factorial(int x, Class1 c) {\n" +
                "        return x > 1 ? x * c.factorial(x - 1, this) : 1;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "intStorage.add(new Calculator().factorial(10));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(3628800));
    }

    @Test
    public void methodRedefineTest() {
        String code =
                "class Test {\n" +
                "    void method(int x, Test t) {}\n" +
                "    int method(int a, Test b) { return 0; }\n" +
                "}\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.MethodAlreadyDeclared, new SingleLineTextRange(3, 9, 55, 6))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void methodReturnPathTest() {
        String code =
                "class Class {\n" +
                "    int inc(int x) {\n" +
                "        if (x > 0) {\n" +
                "            return x + 1;\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "intStorage.add(new Class().inc(5));\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new MultiLineTextRange(2, 20, 6, 6, 33, 64))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void awaitInConstructorTest() {
        String code =
                "class Class {\n" +
                "    constructor() {\n" +
                "        await futures.create();\n" +
                "    }\n" +
                "}\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.AwaitInNonAsyncContext, new SingleLineTextRange(3, 9, 42, 5))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void awaitInMethodTest() {
        String code =
                "class Class {\n" +
                "    void method() {\n" +
                "        await futures.create();\n" +
                "    }\n" +
                "}\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.AwaitInNonAsyncContext, new SingleLineTextRange(3, 9, 42, 5))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void asyncMethodTest1() {
        String code =
                "class Class {\n" +
                "    async int sum2() {\n" +
                "        return await futures.createInt() + await futures.createInt();\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await new Class().sum2());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(0).complete(123);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(1).complete(456);
        Assertions.assertTrue(future.isDone());

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(579));
    }

    @Test
    public void asyncMethodTest2() {
        String code =
                "class Class {\n" +
                "    int f1;\n" +
                "    int f2;\n" +
                "    async int calc(int p1, int p2) {\n" +
                "        let m1 = await futures.createInt();\n" +
                "        let a1 = p1 * this.f1 * m1;\n" +
                "        let m2 = await futures.createInt();\n" +
                "        let a2 = p2 * this.f2 * m2;\n" +
                "        return a1 + a2;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "let c = new Class();\n" +
                "c.f1 = 4;\n" +
                "c.f2 = 5;\n" +
                "intStorage.add(await c.calc(6, 7));\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(0).complete(8);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(1).complete(9);
        Assertions.assertTrue(future.isDone());

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(4 * 6 * 8 + 5 * 7 * 9));
    }

    @Test
    public void arrowConstructorTest() {
        String code =
                "class Class {\n" +
                "    constructor(int x) => this.x = x;\n" +
                "    int x;\n" +
                "}\n" +
                "\n" +
                "let c = new Class(12);\n" +
                "intStorage.add(c.x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(12));
    }

    @Test
    public void constructorInvalidArgumentsTest() {
        String code =
                "class Class {\n" +
                "    constructor(int x) {}\n" +
                "}\n" +
                "\n" +
                "let c = new Class⟦(\"text\")⟧;\n";

        String candidates =
                "Candidates:\n" +
                "constructor Class(int x)";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.ConstructorInvalidArguments,
                "Class", candidates);
    }

    @Test
    public void arrowMethodVoidTest() {
        String code =
                "class Class {\n" +
                "    int x;\n" +
                "    constructor(int x) => this.x = x;\n" +
                "    void inc() => this.x++;\n" +
                "}\n" +
                "\n" +
                "let c = new Class(12);\n" +
                "c.inc();\n" +
                "intStorage.add(c.x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(13));
    }

    @Test
    public void arrowMethodReturnTest() {
        String code =
                "class Class {\n" +
                "    int x;\n" +
                "    constructor(int x) => this.x = x;\n" +
                "    int sqr() => this.x * this.x;\n" +
                "}\n" +
                "\n" +
                "let c = new Class(12);\n" +
                "intStorage.add(c.sqr());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(144));
    }

    @Test
    public void noThisAccessTest() {
        String code =
                "class Class {\n" +
                "    int x;\n" +
                "    constructor(int value) => x = value;\n" +
                "    int sqr() => x * x;\n" +
                "    void inc() => x++;\n" +
                "    int cubic() => sqr() * x;\n" +
                "    int overload() => x + 2;\n" +
                "    int overload(int y) => x + y;\n" +
                "    int overload(int y, int z) => x * y + z;\n" +
                "    fn<() => int> func1() => overload;\n" +
                "    fn<int => int> func2() => overload;\n" +
                "    fn<(int, int) => int> func3() => overload;\n" +
                "}\n" +
                "\n" +
                "let c = new Class(12);\n" +
                "c.inc();\n" +
                "c.inc();\n" +
                "intStorage.add(c.sqr());\n" +
                "intStorage.add(c.cubic());\n" +
                "\n" +
                "let func1 = c.func1();\n" +
                "intStorage.add(func1());\n" +
                "let func2 = c.func2();\n" +
                "intStorage.add(func2(6));\n" +
                "let func3 = c.func3();\n" +
                "intStorage.add(func3(2, 8));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(196, 2744, 16, 20, 36));
    }

    @Test
    public void captureVariablesConstructorTest() {
        String code =
                "class Class {\n" +
                "    fn<int => int> func;\n" +
                "    constructor(int factor) {\n" +
                "        int add = 8;\n" +
                "        func = x => x * factor + add;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "let c = new Class(5);\n" +
                "intStorage.add(c.func(4));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(28));
    }

    @Test
    public void captureVariablesMethodTest() {
        String code =
                "class Class {\n" +
                "    fn<int => int> func;\n" +
                "    void create(int factor) {\n" +
                "        int add = 8;\n" +
                "        func = x => x * factor + add;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "let c = new Class();\n" +
                "c.create(5);\n" +
                "intStorage.add(c.func(4));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(28));
    }

    @Test
    public void defaultValueTest() {
        String code =
                "class Class {}\n" +
                "Class c;\n";

        comparator.assertEquals(
                Lists.of(
                        new DiagnosticMessage(BinderErrors.NoDefaultValue, new SingleLineTextRange(2, 1, 15, 8), "Class")),
                getDiagnostics(ApiRoot.class, code));
    }

    private static void assertVisibility(Member member, int expected) {
        int mask = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;
        Assertions.assertEquals(expected, member.getModifiers() & mask);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static ObjectStorage objectStorage;
        public static FutureHelper futures;
    }
}