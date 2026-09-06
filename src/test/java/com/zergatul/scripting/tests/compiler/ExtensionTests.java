package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.AsyncRunnable;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.*;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.*;

public class ExtensionTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.int64Storage = new Int64Storage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.futures = new FutureHelper();
    }

    @Test
    public void int32Test() {
        String code =
                "extension(int) {\n" +
                "    int next() => this + 1;\n" +
                "    int more(int x) => this + x;\n" +
                "}\n" +
                "\n" +
                "intStorage.add((10).next());\n" +
                "intStorage.add((10).more(5));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(11, 15));
    }

    @Test
    public void int64Test() {
        String code =
                "extension(int64) {\n" +
                "    int64 next() => this + 1;\n" +
                "    int64 more(long x) => this + x;\n" +
                "}\n" +
                "\n" +
                "int64 x = 10;\n" +
                "int64Storage.add(x.next());\n" +
                "int64Storage.add(x.more(5));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.int64Storage.list, Lists.of(11L, 15L));
    }

    @Test
    public void intArrayTest() {
        String code =
                "extension(int[]) {\n" +
                "    boolean contains(int value) {\n" +
                "        for (int i = 0; i < this.length; i++) {\n" +
                "            if (this[i] == value) {\n" +
                "                return true;\n" +
                "            }\n" +
                "        }\n" +
                "        return false;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "boolStorage.add([1].contains(1));\n" +
                "boolStorage.add([2].contains(3));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false));
    }

    @Test
    public void conflictWithInstanceMethodTest() {
        String code =
                "extension(MyClass) {\n" +
                "    void myMethod() {}\n" +
                "}\n" +
                "class MyClass {\n" +
                "    void myMethod() {}\n" +
                "}\n";

        comparator.assertEquals(Lists.of(
                        new DiagnosticMessage(
                                BinderErrors.MethodAlreadyDeclared,
                                new SingleLineTextRange(2, 10, 30, 8))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void conflictWithExtensionMethodTest() {
        String code =
                "extension(MyClass) {\n" +
                "    void myMethod(long[] array) {}\n" +
                "    void lol(string[] strings) {}\n" +
                "}\n" +
                "class MyClass {\n" +
                "    void myMethod(int[] array) {}\n" +
                "}\n" +
                "extension(MyClass) {\n" +
                "    int lol(string[] strings) => 1;\n" +
                "}\n";

        comparator.assertEquals(Lists.of(
                        new DiagnosticMessage(
                                BinderErrors.ExtensionMethodAlreadyDeclared,
                                new SingleLineTextRange(9, 9, 173, 3))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void extensionFromClassTest() {
        String code =
                "extension(int) {\n" +
                "    int next() => this + 1;\n" +
                "}\n" +
                "class Test {\n" +
                "    int inc(int value) => value.next();\n" +
                "}\n" +
                "\n" +
                "intStorage.add(new Test().inc(123));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(124));
    }

    @Test
    public void asyncVoidTest() {
        String code =
                "extension(int) {\n" +
                "    async void wait() {\n" +
                "        for (int i = 0; i < this; i++) {\n" +
                "            await futures.create();\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "await (3).wait();\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.get(0).complete(null);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.get(1).complete(null);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.get(2).complete(null);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void asyncValueTest() {
        String code =
                "extension(int) {\n" +
                "    async int sum() {\n" +
                "        int result = 0;\n" +
                "        for (int i = 0; i < this; i++) {\n" +
                "            result += await futures.createInt();\n" +
                "        }\n" +
                "        return result;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await (3).sum());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(0).complete(3);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(1).complete(4);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(2).complete(5);
        Assertions.assertTrue(future.isDone());
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(12));
    }

    @Test
    public void capturingVariableTest() {
        String code =
                "extension(int) {\n" +
                "    fn<int => int> createMultiplier() {\n" +
                "        int self = this;\n" +
                "        return x => x * self;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "let mult0 = (0).createMultiplier();\n" +
                "let mult1 = (1).createMultiplier();\n" +
                "let mult3 = (3).createMultiplier();\n" +
                "intStorage.add(mult0(5));\n" +
                "intStorage.add(mult1(5));\n" +
                "intStorage.add(mult3(5));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(0, 5, 15));
    }

    @Test
    public void capturingParameterTest() {
        String code =
                "extension(int) {\n" +
                "    fn<int => int> createLinearFunction(int add) {\n" +
                "        int self = this;\n" +
                "        return x => x * self + add;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "let func = (10).createLinearFunction(7);\n" +
                "intStorage.add(func(1));\n" +
                "intStorage.add(func(2));\n" +
                "intStorage.add(func(3));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(17, 27, 37));
    }

    @Test
    public void javaTypeTest() {
        String code =
                "extension(Java<java.util.Hashtable>) {\n" +
                "    int getInt(string key) {\n" +
                "        let value = this.get(key);\n" +
                "        if (value is int) {\n" +
                "            return value as int;\n" +
                "        } else {\n" +
                "            return 0;\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "let table = new Java<java.util.Hashtable>();\n" +
                "table.put(\"key1\", 123);\n" +
                "table.put(\"key2\", \"\");\n" +
                "intStorage.add(table.getInt(\"key1\"));\n" +
                "intStorage.add(table.getInt(\"key2\"));\n" +
                "intStorage.add(table.getInt(\"key3\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(123, 0, 0));
    }

    @Test
    public void classInheritanceTest() {
        String code =
                "extension(Java<java.util.Dictionary>) {\n" +
                "    int getInt(string key) {\n" +
                "        let value = this.get(key);\n" +
                "        if (value is int) {\n" +
                "            return value as int;\n" +
                "        } else {\n" +
                "            return 0;\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "let table = new Java<java.util.Hashtable>();\n" +
                "table.put(\"key1\", 123);\n" +
                "table.put(\"key2\", \"\");\n" +
                "intStorage.add(table.getInt(\"key1\"));\n" +
                "intStorage.add(table.getInt(\"key2\"));\n" +
                "intStorage.add(table.getInt(\"key3\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(123, 0, 0));
    }

    @Test
    public void interfaceInheritanceTest() {
        String code =
                "extension(Java<java.util.Map>) {\n" +
                "    int getInt(string key) {\n" +
                "        let value = this.get(key);\n" +
                "        if (value is int) {\n" +
                "            return value as int;\n" +
                "        } else {\n" +
                "            return 0;\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "let table = new Java<java.util.Hashtable>();\n" +
                "table.put(\"key1\", 123);\n" +
                "table.put(\"key2\", \"\");\n" +
                "intStorage.add(table.getInt(\"key1\"));\n" +
                "intStorage.add(table.getInt(\"key2\"));\n" +
                "intStorage.add(table.getInt(\"key3\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(123, 0, 0));
    }

    @Test
    public void unaryOperationTest() {
        String code =
                "extension(string) {\n" +
                "    operator [+] int(string str) {\n" +
                "        int value;\n" +
                "        if (int.tryParse(str, ref value)) {\n" +
                "            return value;\n" +
                "        } else {\n" +
                "            return int.MIN_VALUE;\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "intStorage.add(+\"\");\n" +
                "intStorage.add(+\"100\");\n" +
                "intStorage.add(+\"123\");\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(Integer.MIN_VALUE, 100, 123));
    }

    @Test
    public void binaryOperationTest() {
        String code =
                "extension(string) {\n" +
                "    operator [/] string[](string str, char ch) => str.split(ch);\n" +
                "}\n" +
                "\n" +
                "let str = \"hello world! bye world!\";\n" +
                "let parts = str / ' ';\n" +
                "foreach (let part in parts) {\n" +
                "    stringStorage.add(part);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("hello", "world!", "bye", "world!"));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static Int64Storage int64Storage;
        public static StringStorage stringStorage;
        public static FutureHelper futures;
    }
}