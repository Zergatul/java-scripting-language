package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.AsyncRunnable;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.FutureHelper;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compileAsync;

public class AsyncFunctionTests {

    @BeforeEach
    public void clean() {
        ApiRoot.futures = new FutureHelper();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void noAwaitTest() {
        String code = """
                async void func() {
                    intStorage.add(123);
                }
                
                await func();
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void voidTest() {
        String code = """
                async void func() {
                    await futures.create();
                    intStorage.add(123);
                }
                
                await func();
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void intTest() {
        String code = """
                async int func() {
                    await futures.create();
                    return 20;
                }
                
                intStorage.add(await func());
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(20));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void floatTest() {
        String code = """
                async float func() {
                    await futures.create();
                    return 20;
                }
                
                floatStorage.add(await func());
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, List.of(20.0));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void stringTest() {
        String code = """
                async string func() {
                    await futures.create();
                    return "test";
                }
                
                stringStorage.add(await func());
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("test"));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void forLoopSyncReturn() {
        String code = """
                async int func() {
                    for (int i = 0; i < 100; i++) {
                        if (i == 2) return i;
                    }
                    return 0;
                }
                
                intStorage.add(await func());
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertTrue(future.isDone());
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2));
    }

    @Test
    public void forLoopAsyncReturn() {
        String code = """
                async void func() {
                    for (int i = 0; i < 100; i++) {
                        await futures.create();
                        if (i == 2) return;
                    }
                }
                
                await func();
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
    public void forEachLoopSyncReturn() {
        String code = """
                async int func() {
                    foreach (int i in new int[] { 0, 1, 2, 3, 4, 5 }) {
                        if (i == 2) return i;
                    }
                    return 0;
                }
                
                intStorage.add(await func());
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertTrue(future.isDone());
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2));
    }

    @Test
    public void forEachLoopAsyncReturn() {
        String code = """
                async int func() {
                    foreach (int i in new int[] { 0, 1, 2, 3, 4, 5 }) {
                        await futures.create();
                        if (i == 2) return i;
                    }
                    return 0;
                }
                
                intStorage.add(await func());
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
    public void whileLoopSyncReturn() {
        String code = """
                async int func() {
                    int i = 0;
                    while (i < 100) {
                        if (i == 2) return i;
                        i++;
                    }
                    return 0;
                }
                
                intStorage.add(await func());
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertTrue(future.isDone());
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2));
    }

    @Test
    public void whileLoopAsyncReturn() {
        String code = """
                async int func() {
                    int i = 0;
                    while (i < 100) {
                        await futures.create();
                        if (i == 2) return i;
                        i++;
                    }
                    return 0;
                }
                
                intStorage.add(await func());
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
    public void parametersTest() {
        String code = """
                async int func(int x) {
                    return await futures.createInt() + x;
                }
                
                intStorage.add(await func(100));
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(23);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123));
        Assertions.assertTrue(future.isDone());
    }

    public static class ApiRoot {
        public static FutureHelper futures;
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
    }
}