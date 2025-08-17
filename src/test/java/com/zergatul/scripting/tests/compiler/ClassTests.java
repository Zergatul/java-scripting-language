package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.AsyncRunnable;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.FutureHelper;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.ObjectStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.*;

public class ClassTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.objectStorage = new ObjectStorage();
        ApiRoot.futures = new FutureHelper();
    }

    @Test
    public void basicTest() {
        String code = """
                class Class{}
                
                Class x = new Class();
                stringStorage.add(#typeof(x).name);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("Class"));
    }

    @Test
    public void functionTest() {
        String code = """
                Class func(Class c) { return c; }
                class Class {}
                
                stringStorage.add(#typeof(func(new Class())).name);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("Class"));
    }

    @Test
    public void staticVariableTest() {
        String code = """
                static Class cc = new Class();
                class Class {}
                
                stringStorage.add(#typeof(cc).name);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("Class"));
    }

    @Test
    public void classRedefineTest() {
        String code = """
                class Class {}
                class Class {}
                
                Class c = new Class();
                """;

        List<DiagnosticMessage> diagnostics = getDiagnostics(ApiRoot.class, code);

        Assertions.assertIterableEquals(diagnostics, List.of(
                new DiagnosticMessage(BinderErrors.SymbolAlreadyDeclared, new SingleLineTextRange(2, 7, 21, 5), "Class")));
    }

    @Test
    public void fieldTest() {
        String code = """
                class Class {
                    int x;
                }
                
                Class c = new Class();
                c.x = 123;
                c.x++;
                c.x += 2;
                intStorage.add(c.x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(126));
    }

    @Test
    public void selfReferenceTest() {
        String code = """
                class ListItem {
                    ListItem next;
                }
                
                int len(ListItem item) {
                    if (item.next is ListItem) {
                        return len(item.next) + 1;
                    } else {
                        return 1;
                    }
                }
                
                let items = new ListItem[10];
                for (let i = 0; i < items.length; i++) {
                    items[i] = new ListItem();
                }
                
                for (let i = 0; i < items.length - 1; i++) {
                    items[i].next = items[i + 1];
                }
                
                intStorage.add(len(items[0]));
                intStorage.add(len(items[1]));
                intStorage.add(len(items[5]));
                intStorage.add(len(items[9]));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 9, 5, 1));
    }

    @Test
    public void fieldRedefineTest() {
        String code = """
                class Item {
                    int val;
                    int val;
                }
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);

        Assertions.assertIterableEquals(
                messages,
                List.of(
                        new DiagnosticMessage(BinderErrors.MemberAlreadyDeclared, new SingleLineTextRange(3, 9, 34, 3), "val")));
    }

    @Test
    public void castToObjectTest() {
        String code = """
                class Class {}
                
                objectStorage.add(new Class());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertEquals(ApiRoot.objectStorage.list.size(), 1);
        Assertions.assertEquals(ApiRoot.objectStorage.list.get(0).getClass().getSimpleName(), "Class");
    }

    @Test
    public void constructorTest1() {
        String code = """
                class Class {
                    constructor(int x) {
                        this.x = x;
                    }
                    int x;
                }
                
                let c = new Class(10);
                intStorage.add(c.x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10));
    }

    @Test
    public void constructorTest2() {
        String code = """
                class Class {
                    constructor(boolean b, int x) {
                        if (b) {
                            this.x = x + x;
                        } else {
                            this.x = x * x;
                        }
                    }
                    int x;
                }
                
                let c1 = new Class(false, 10);
                let c2 = new Class(true, 20);
                intStorage.add(c1.x);
                intStorage.add(c2.x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(100, 40));
    }

    @Test
    public void constructorTest3() {
        String code = """
                int sum(TreeEntry entry) {
                    int total = entry.value;
                    if (entry.left is TreeEntry) {
                        total += entry.left.value;
                    }
                    if (entry.right is TreeEntry) {
                        total += entry.right.value;
                    }
                    return total;
                }
                
                class TreeEntry {
                    constructor(int value) {
                        this.value = value;
                    }
                    constructor(TreeEntry left, TreeEntry right, int value) {
                        this.left = left;
                        this.right = right;
                        this.value = value;
                    }
                    TreeEntry left;
                    TreeEntry right;
                    int value;
                }
                
                let tree = new TreeEntry(
                    new TreeEntry(10),
                    new TreeEntry(20),
                    50);
                intStorage.add(sum(tree));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(80));
    }

    @Test
    public void constructorTest4() {
        String code = """
                class Fib {
                    constructor(int value) {
                        if (value <= 2) {
                            this.value = 1;
                        } else {
                            this.value = new Fib(value - 1).value + new Fib(value - 2).value;
                        }
                    }
                    int value;
                }
                
                intStorage.add(new Fib(8).value);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(21));
    }

    @Test
    public void constructorRedefineTest() {
        String code = """
                class Test {
                    constructor(Test s1, Test s2) {}
                    constructor(Test t1, Test t2) {}
                }
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);

        Assertions.assertIterableEquals(
                messages,
                List.of(
                        new DiagnosticMessage(BinderErrors.ConstructorAlreadyDeclared, new SingleLineTextRange(3, 5, 54, 11))));
    }

    @Test
    public void methodTest1() {
        String code = """
                class Class {
                    void add(int x) {
                        intStorage.add(x);
                    }
                }
                
                let c = new Class();
                c.add(5);
                c.add(3);
                c.add(1);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(5, 3, 1));
    }

    @Test
    public void methodTest2() {
        String code = """
                class Class {
                    void add(int x) {
                        intStorage.add(x);
                    }
                    void add(int x, int y) {
                        intStorage.add(x + y);
                    }
                    void add(int x, int y, int z) {
                        intStorage.add(x + y + z);
                    }
                    void add(string s) {
                        stringStorage.add(s);
                    }
                }
                
                let c = new Class();
                c.add(5);
                c.add(3, 5);
                c.add(1, 3, 5);
                c.add("qq");
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(5, 8, 9));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("qq"));
    }

    @Test
    public void methodTest3() {
        String code = """
                class Class {
                    int inc(int x) {
                        return x + 1;
                    }
                }
                
                intStorage.add(new Class().inc(5));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(6));
    }

    @Test
    public void methodTest4() {
        String code = """
                class Class {
                    int factorial1(int x) {
                        return x > 1 ? x * this.factorial2(x - 1) : 1;
                    }
                    int factorial2(int x) {
                        return x > 1 ? x * this.factorial3(x - 1) : 1;
                    }
                    int factorial3(int x) {
                        return x > 1 ? x * this.factorial1(x - 1) : 1;
                    }
                }
                
                intStorage.add(new Class().factorial1(10));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(3628800));
    }

    @Test
    public void methodTest5() {
        String code = """
                class Calculator {
                    int factorial(int x) {
                        let c1 = new Class1();
                        let c2 = new Class2();
                        if (x % 2 == 0) {
                            return c1.factorial(x, c2);
                        } else {
                            return c2.factorial(x, c1);
                        }
                    }
                }
                class Class1 {
                    int factorial(int x, Class2 c) {
                        return x > 1 ? x * c.factorial(x - 1, this) : 1;
                    }
                }
                class Class2 {
                    int factorial(int x, Class1 c) {
                        return x > 1 ? x * c.factorial(x - 1, this) : 1;
                    }
                }
                
                intStorage.add(new Calculator().factorial(10));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(3628800));
    }

    @Test
    public void methodRedefineTest() {
        String code = """
                class Test {
                    void method(int x, Test t) {}
                    int method(int a, Test b) { return 0; }
                }
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);

        Assertions.assertIterableEquals(
                messages,
                List.of(
                        new DiagnosticMessage(BinderErrors.MethodAlreadyDeclared, new SingleLineTextRange(3, 9, 55, 6))));
    }

    @Test
    public void methodReturnPathTest() {
        String code = """
                class Class {
                    int inc(int x) {
                        if (x > 0) {
                            return x + 1;
                        }
                    }
                }
                
                intStorage.add(new Class().inc(5));
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);

        Assertions.assertIterableEquals(
                messages,
                List.of(
                        new DiagnosticMessage(BinderErrors.NotAllPathReturnValue, new MultiLineTextRange(2, 20, 6, 6, 33, 64))));
    }

    @Test
    public void awaitInConstructorTest() {
        String code = """
                class Class {
                    constructor() {
                        await futures.create();
                    }
                }
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);

        Assertions.assertIterableEquals(
                messages,
                List.of(
                        new DiagnosticMessage(BinderErrors.AwaitInNonAsyncContext, new SingleLineTextRange(3, 9, 42, 5))));
    }

    @Test
    public void awaitInMethodTest() {
        String code = """
                class Class {
                    void method() {
                        await futures.create();
                    }
                }
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);

        Assertions.assertIterableEquals(
                messages,
                List.of(
                        new DiagnosticMessage(BinderErrors.AwaitInNonAsyncContext, new SingleLineTextRange(3, 9, 42, 5))));
    }

    @Test
    public void asyncMethodTest1() {
        String code = """
                class Class {
                    async int sum2() {
                        return await futures.createInt() + await futures.createInt();
                    }
                }
                
                intStorage.add(await new Class().sum2());
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(0).complete(123);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(1).complete(456);
        Assertions.assertTrue(future.isDone());

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(579));
    }

    @Test
    public void asyncMethodTest2() {
        String code = """
                class Class {
                    int f1;
                    int f2;
                    async int calc(int p1, int p2) {
                        let m1 = await futures.createInt();
                        let a1 = p1 * this.f1 * m1;
                        let m2 = await futures.createInt();
                        let a2 = p2 * this.f2 * m2;
                        return a1 + a2;
                    }
                }
                
                let c = new Class();
                c.f1 = 4;
                c.f2 = 5;
                intStorage.add(await c.calc(6, 7));
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(0).complete(8);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(1).complete(9);
        Assertions.assertTrue(future.isDone());

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(4 * 6 * 8 + 5 * 7 * 9));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static ObjectStorage objectStorage;
        public static FutureHelper futures;
    }
}