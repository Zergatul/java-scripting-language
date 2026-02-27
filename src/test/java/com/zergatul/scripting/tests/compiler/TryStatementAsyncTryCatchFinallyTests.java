package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.AsyncRunnable;
import com.zergatul.scripting.tests.compiler.helpers.FutureHelper;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compileAsync;

public class TryStatementAsyncTryCatchFinallyTests {

    @BeforeEach
    public void clean() {
        ApiRoot.futures = new FutureHelper();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void simpleTest() {
        String code = """
                try {
                    intStorage.add(1);
                    await futures.create();
                    intStorage.add(2);
                } catch {
                    intStorage.add(3);
                } finally {
                    intStorage.add(4);
                }
                intStorage.add(5);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 4, 5));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void exceptionTest() {
        String code = """
                try {
                    intStorage.add(1);
                    await futures.create();
                    intStorage.add(2);
                    [1][2] = 3; // throws
                    intStorage.add(3);
                } catch {
                    intStorage.add(4);
                } finally {
                    intStorage.add(5);
                }
                intStorage.add(6);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 4, 5, 6));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void innerBlockTest() {
        String code = """
                try {
                    intStorage.add(1);
                    await futures.create();
                    intStorage.add(2);
                    try {
                        intStorage.add(3);
                        [1][2] = 3; // throws
                        intStorage.add(999);
                    } catch {
                        intStorage.add(4);
                        throw;
                    } finally {
                        intStorage.add(5);
                    }
                    intStorage.add(6);
                } catch {
                    intStorage.add(7);
                } finally {
                    intStorage.add(8);
                }
                intStorage.add(9);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3, 4, 5, 7, 8, 9), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Disabled("Not supported by current state machine generator")
    @Test
    public void jumpFromCatchOuterFinallyTest() {
        String code = """
                for (int i = 0; i < 3; i++) {
                    try {
                        try {
                            intStorage.add(i);
                            await futures.create();          // F0, F2, F4...
                            if (i == 0) {
                                intStorage.add(10);
                            }
                        } finally {
                            intStorage.add(100 + i);
                            await futures.create();          // F1, F3, F5...
                            if (i == 0) {
                                [1][2] = 3;                  // throws
                            }
                            intStorage.add(200 + i);
                        }
                    } catch {
                        intStorage.add(300 + i);
                        continue;
                    } finally {
                        intStorage.add(400 + i);
                        await futures.create();              // F? (only for iterations that reach here)
                        intStorage.add(500 + i);
                    }
                    intStorage.add(600 + i);
                }
                intStorage.add(9999);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(0), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(0, 10, 100), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(List.of(0, 10, 100, 300, 400), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(List.of(0, 10, 100, 300, 400, 500, 1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());


        //Assertions.assertTrue(future.isDone());
    }

    public static class ApiRoot {
        public static FutureHelper futures;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}