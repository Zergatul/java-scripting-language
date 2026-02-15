package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.AsyncRunnable;
import com.zergatul.scripting.tests.compiler.helpers.FutureHelper;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

        Assertions.assertIterableEquals(List.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 4, 5), ApiRoot.intStorage.list);
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

        Assertions.assertIterableEquals(List.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 4, 5, 6), ApiRoot.intStorage.list);
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

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3, 4, 5, 7, 8, 9));
        Assertions.assertTrue(future.isDone());
    }

    public static class ApiRoot {
        public static FutureHelper futures;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}