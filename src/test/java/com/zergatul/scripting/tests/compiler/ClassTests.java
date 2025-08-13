package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.ObjectStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class ClassTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.objectStorage = new ObjectStorage();
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

    // TODO: method  redefine

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

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static ObjectStorage objectStorage;
    }
}