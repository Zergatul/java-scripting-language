package com.zergatul.scripting.tests.compiler;

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
        String code = """
                extension(int) {
                    int next() => this + 1;
                    int more(int x) => this + x;
                }
                
                intStorage.add((10).next());
                intStorage.add((10).more(5));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11, 15));
    }

    @Test
    public void int64Test() {
        String code = """
                extension(int64) {
                    int64 next() => this + 1;
                    int64 more(long x) => this + x;
                }
                
                int64 x = 10;
                int64Storage.add(x.next());
                int64Storage.add(x.more(5));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.int64Storage.list, List.of(11L, 15L));
    }

    @Test
    public void intArrayTest() {
        String code = """
                extension(int[]) {
                    boolean contains(int value) {
                        for (int i = 0; i < this.length; i++) {
                            if (this[i] == value) {
                                return true;
                            }
                        }
                        return false;
                    }
                }
                
                boolStorage.add([1].contains(1));
                boolStorage.add([2].contains(3));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false));
    }

    @Test
    public void conflictWithInstanceMethodTest() {
        String code = """
                extension(MyClass) {
                    void myMethod() {}
                }
                class MyClass {
                    void myMethod() {}
                }
                """;

        comparator.assertEquals(List.of(
                        new DiagnosticMessage(
                                BinderErrors.MethodAlreadyDeclared,
                                new SingleLineTextRange(2, 10, 30, 8))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void conflictWithExtensionMethodTest() {
        String code = """
                extension(MyClass) {
                    void myMethod(long[] array) {}
                    void lol(string[] strings) {}
                }
                class MyClass {
                    void myMethod(int[] array) {}
                }
                extension(MyClass) {
                    int lol(string[] strings) => 1;
                }
                """;

        comparator.assertEquals(List.of(
                        new DiagnosticMessage(
                                BinderErrors.ExtensionMethodAlreadyDeclared,
                                new SingleLineTextRange(9, 9, 173, 3))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void extensionFromClassTest() {
        String code = """
                extension(int) {
                    int next() => this + 1;
                }
                class Test {
                    int inc(int value) => value.next();
                }
                
                intStorage.add(new Test().inc(123));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(124));
    }

    @Test
    public void asyncVoidTest() {
        String code = """
                extension(int) {
                    async void wait() {
                        for (int i = 0; i < this; i++) {
                            await futures.create();
                        }
                    }
                }
                
                await (3).wait();
                """;

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
        String code = """
                extension(int) {
                    async int sum() {
                        int result = 0;
                        for (int i = 0; i < this; i++) {
                            result += await futures.createInt();
                        }
                        return result;
                    }
                }
                
                intStorage.add(await (3).sum());
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(0).complete(3);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(1).complete(4);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(2).complete(5);
        Assertions.assertTrue(future.isDone());
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(12));
    }

    @Test
    public void capturingVariableTest() {
        String code = """
                extension(int) {
                    fn<int => int> createMultiplier() {
                        int self = this;
                        return x => x * self;
                    }
                }
                
                let mult0 = (0).createMultiplier();
                let mult1 = (1).createMultiplier();
                let mult3 = (3).createMultiplier();
                intStorage.add(mult0(5));
                intStorage.add(mult1(5));
                intStorage.add(mult3(5));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(0, 5, 15));
    }

    @Test
    public void capturingParameterTest() {
        String code = """
                extension(int) {
                    fn<int => int> createLinearFunction(int add) {
                        int self = this;
                        return x => x * self + add;
                    }
                }
                
                let func = (10).createLinearFunction(7);
                intStorage.add(func(1));
                intStorage.add(func(2));
                intStorage.add(func(3));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(17, 27, 37));
    }

    @Test
    public void javaTypeTest() {
        String code = """
                extension(Java<java.util.Hashtable>) {
                    int getInt(string key) {
                        let value = this.get(key);
                        if (value is int) {
                            return value as int;
                        } else {
                            return 0;
                        }
                    }
                }
                
                let table = new Java<java.util.Hashtable>();
                table.put("key1", 123);
                table.put("key2", "");
                intStorage.add(table.getInt("key1"));
                intStorage.add(table.getInt("key2"));
                intStorage.add(table.getInt("key3"));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123, 0, 0));
    }

    @Test
    public void classInheritanceTest() {
        String code = """
                extension(Java<java.util.Dictionary>) {
                    int getInt(string key) {
                        let value = this.get(key);
                        if (value is int) {
                            return value as int;
                        } else {
                            return 0;
                        }
                    }
                }
                
                let table = new Java<java.util.Hashtable>();
                table.put("key1", 123);
                table.put("key2", "");
                intStorage.add(table.getInt("key1"));
                intStorage.add(table.getInt("key2"));
                intStorage.add(table.getInt("key3"));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123, 0, 0));
    }

    @Test
    public void interfaceInheritanceTest() {
        String code = """
                extension(Java<java.util.Map>) {
                    int getInt(string key) {
                        let value = this.get(key);
                        if (value is int) {
                            return value as int;
                        } else {
                            return 0;
                        }
                    }
                }
                
                let table = new Java<java.util.Hashtable>();
                table.put("key1", 123);
                table.put("key2", "");
                intStorage.add(table.getInt("key1"));
                intStorage.add(table.getInt("key2"));
                intStorage.add(table.getInt("key3"));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123, 0, 0));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static Int64Storage int64Storage;
        public static StringStorage stringStorage;
        public static FutureHelper futures;
    }
}