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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.*;

public class AsyncFunctionTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.futures = new FutureHelper();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.int64Storage = new Int64Storage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void noAwaitTest() {
        String code =
                "async void func() {\n" +
                "    intStorage.add(123);\n" +
                "}\n" +
                "\n" +
                "await func();\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(123));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void voidTest() {
        String code =
                "async void func() {\n" +
                "    await futures.create();\n" +
                "    intStorage.add(123);\n" +
                "}\n" +
                "\n" +
                "await func();\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(123));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void cancellationCascadesIntoNestedAsyncFunctionTest() {
        String code =
                "async void func() {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(2);\n" +
                "}\n" +
                "\n" +
                "await func();\n" +
                "intStorage.add(3);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertTrue(future.cancel(false));
        Assertions.assertTrue(ApiRoot.futures.get(0).isCancelled());
        Assertions.assertFalse(ApiRoot.futures.get(0).complete(null));
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
    }

    @Test
    public void intTest() {
        String code =
                "async int func() {\n" +
                "    await futures.create();\n" +
                "    return 20;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await func());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(20));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void floatTest() {
        String code =
                "async float func() {\n" +
                "    await futures.create();\n" +
                "    return 20;\n" +
                "}\n" +
                "\n" +
                "floatStorage.add(await func());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, Lists.of(20.0));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void stringTest() {
        String code =
                "async string func() {\n" +
                "    await futures.create();\n" +
                "    return \"test\";\n" +
                "}\n" +
                "\n" +
                "stringStorage.add(await func());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("test"));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void forLoopSyncReturn() {
        String code =
                "async int func() {\n" +
                "    for (int i = 0; i < 100; i++) {\n" +
                "        if (i == 2) return i;\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await func());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertTrue(future.isDone());
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(2));
    }

    @Test
    public void forLoopAsyncReturn() {
        String code =
                "async void func() {\n" +
                "    for (int i = 0; i < 100; i++) {\n" +
                "        await futures.create();\n" +
                "        if (i == 2) return;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "await func();\n";

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
    public void forEachLoopSyncReturn() {
        String code =
                "async int func() {\n" +
                "    foreach (int i in new int[] { 0, 1, 2, 3, 4, 5 }) {\n" +
                "        if (i == 2) return i;\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await func());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertTrue(future.isDone());
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(2));
    }

    @Test
    public void forEachLoopAsyncReturn() {
        String code =
                "async int func() {\n" +
                "    foreach (int i in new int[] { 0, 1, 2, 3, 4, 5 }) {\n" +
                "        await futures.create();\n" +
                "        if (i == 2) return i;\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await func());\n";

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
    public void whileLoopSyncReturn() {
        String code =
                "async int func() {\n" +
                "    int i = 0;\n" +
                "    while (i < 100) {\n" +
                "        if (i == 2) return i;\n" +
                "        i++;\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await func());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertTrue(future.isDone());
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(2));
    }

    @Test
    public void whileLoopAsyncReturn() {
        String code =
                "async int func() {\n" +
                "    int i = 0;\n" +
                "    while (i < 100) {\n" +
                "        await futures.create();\n" +
                "        if (i == 2) return i;\n" +
                "        i++;\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await func());\n";

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
    public void parametersTest1() {
        String code =
                "async int func(int x) {\n" +
                "    return await futures.createInt() + x;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await func(100));\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(23);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(123));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void parametersTest2() {
        String code =
                "async float func(float a, float b) {\n" +
                "    return await futures.createFloat() + a * a + b * b * b;\n" +
                "}\n" +
                "\n" +
                "floatStorage.add(await func(1.5, 2));\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getFloat(0).complete(0.0125);
        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, Lists.of(10.2625));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void parametersTest3() {
        String code =
                "async long func(int x1, long x2, int x3) {\n" +
                "    return await futures.createInt() + x1 + x2 + x3;\n" +
                "}\n" +
                "\n" +
                "int64Storage.add(await func(100, 200, 300));\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(23);
        Assertions.assertIterableEquals(ApiRoot.int64Storage.list, Lists.of(623L));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void nonAsyncMainContextTest() {
        String code =
                "async int func1(int x) {\n" +
                "    return await futures.createInt() + x;\n" +
                "}\n" +
                "\n" +
                "async void func2() {\n" +
                "    intStorage.add(await func1(10) + await func1(100));\n" +
                "}\n" +
                "\n" +
                "func2();\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertTrue(ApiRoot.intStorage.list.isEmpty());

        ApiRoot.futures.getInt(0).complete(1);
        ApiRoot.futures.getInt(1).complete(2);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(113));
    }

    @Test
    public void privateFieldFromAsyncClassMethodTest() {
        String code =
                "class Class {\n" +
                "    private int value;\n" +
                "\n" +
                "    constructor(int value) {\n" +
                "        this.value = value;\n" +
                "    }\n" +
                "\n" +
                "    async int getValue() => value;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await new Class(17).getValue());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertDoesNotThrow(future::join);
        Assertions.assertIterableEquals(Lists.of(17), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedFieldFromAsyncClassMethodTest() {
        String code =
                "class Class {\n" +
                "    protected int value;\n" +
                "\n" +
                "    constructor(int value) {\n" +
                "        this.value = value;\n" +
                "    }\n" +
                "\n" +
                "    async int getValue() => value;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await new Class(23).getValue());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertDoesNotThrow(future::join);
        Assertions.assertIterableEquals(Lists.of(23), ApiRoot.intStorage.list);
    }

    @Test
    public void privateMethodFromAsyncClassMethodTest() {
        String code =
                "class Class {\n" +
                "    private int getValue() => 31;\n" +
                "\n" +
                "    async int execute() => getValue();\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await new Class().execute());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertDoesNotThrow(future::join);
        Assertions.assertIterableEquals(Lists.of(31), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedMethodFromAsyncClassMethodTest() {
        String code =
                "class Class {\n" +
                "    protected int getValue() => 37;\n" +
                "\n" +
                "    async int execute() => getValue();\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await new Class().execute());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertDoesNotThrow(future::join);
        Assertions.assertIterableEquals(Lists.of(37), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaFieldFromAsyncClassMethodTest() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.AsyncFunctionTests$ProtectedBase> {\n" +
                "    async int getValue() => value;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await new Class().getValue());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertDoesNotThrow(future::join);
        Assertions.assertIterableEquals(Lists.of(43), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaMethodFromAsyncClassMethodTest() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.AsyncFunctionTests$ProtectedBase> {\n" +
                "    async int execute() => getProtectedValue();\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await new Class().execute());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertDoesNotThrow(future::join);
        Assertions.assertIterableEquals(Lists.of(47), ApiRoot.intStorage.list);
    }

    @Test
    public void protectedJavaBaseMethodFromAsyncClassMethodTest() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.AsyncFunctionTests$ProtectedBase> {\n" +
                "    async int execute() => base.getProtectedValue();\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await new Class().execute());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertDoesNotThrow(future::join);
        Assertions.assertIterableEquals(Lists.of(47), ApiRoot.intStorage.list);
    }

    @Test
    public void nonPublicMembersAfterAwaitFromAsyncClassMethodTest() {
        String code =
                "class Class : Java<com.zergatul.scripting.tests.compiler.AsyncFunctionTests$ProtectedBase> {\n" +
                "    private int ownValue;\n" +
                "\n" +
                "    constructor(int value) {\n" +
                "        ownValue = value;\n" +
                "    }\n" +
                "\n" +
                "    private int getOwnValue() => ownValue + 2;\n" +
                "\n" +
                "    async int execute() {\n" +
                "        await futures.create();\n" +
                "        return ownValue + getOwnValue() + value + base.getProtectedValue();\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await new Class(5).execute());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.get(0).complete(null);
        Assertions.assertDoesNotThrow(future::join);
        Assertions.assertIterableEquals(Lists.of(102), ApiRoot.intStorage.list);
    }

    @Test
    public void privateConstructorFromAsyncClassMethodTest() {
        String code =
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
                "    async int createAndGetValue() => new Class(41).getValue();\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await new Class().createAndGetValue());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertDoesNotThrow(future::join);
        Assertions.assertIterableEquals(Lists.of(41), ApiRoot.intStorage.list);
    }

    @Test
    public void privateAndProtectedMembersFromNestedLambdaAfterAwaitTest() {
        String code =
                "typealias Run = Java<com.zergatul.scripting.tests.compiler.helpers.Run>;\n" +
                "\n" +
                "class Class : Java<com.zergatul.scripting.tests.compiler.AsyncFunctionTests$ProtectedBase> {\n" +
                "    private int ownValue;\n" +
                "\n" +
                "    async int execute() {\n" +
                "        await futures.create();\n" +
                "        let self = this;\n" +
                "        new Run().once(() => {\n" +
                "            new Run().once(() => {\n" +
                "                self.ownValue += self.value;\n" +
                "                self.ownValue += self.getProtectedValue();\n" +
                "            });\n" +
                "        });\n" +
                "        return ownValue;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await new Class().execute());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.get(0).complete(null);
        Assertions.assertDoesNotThrow(future::join);
        Assertions.assertIterableEquals(Lists.of(90), ApiRoot.intStorage.list);
    }

    @Test
    public void privateMembersFromLambdaSurvivingAwaitTest() {
        String code =
                "typealias Runnable = Java<java.lang.Runnable>;\n" +
                "\n" +
                "class Class {\n" +
                "    private int value;\n" +
                "\n" +
                "    private void setValue(int value) {\n" +
                "        this.value = value;\n" +
                "    }\n" +
                "\n" +
                "    async int execute() {\n" +
                "        let self = this;\n" +
                "        Runnable action = () => self.setValue(73);\n" +
                "        await futures.create();\n" +
                "        action.run();\n" +
                "        return value;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await new Class().execute());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.get(0).complete(null);
        Assertions.assertDoesNotThrow(future::join);
        Assertions.assertIterableEquals(Lists.of(73), ApiRoot.intStorage.list);
    }

    @Test
    public void missingAwaitTest() {
        String code =
                "async int count() => 1;\n" +
                "int x = count();\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.CannotImplicitlyConvert, new SingleLineTextRange(2, 9, 32, 7), "Future<int>", "int")),
                getDiagnostics(ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static FutureHelper futures;
        public static IntStorage intStorage;
        public static Int64Storage int64Storage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
    }

    @SuppressWarnings("unused")
    public static class ProtectedBase {
        protected int value = 43;

        protected int getProtectedValue() {
            return 47;
        }
    }
}