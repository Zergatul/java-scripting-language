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

public class TryStatementAsyncTryFinallyTests {

    @BeforeEach
    public void clean() {
        ApiRoot.futures = new FutureHelper();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void noExceptionTest() {
        String code = """
                try {
                    intStorage.add(1);
                    await futures.create();
                    intStorage.add(2);
                } finally {
                    intStorage.add(3);
                }
                intStorage.add(4);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3, 4), ApiRoot.intStorage.list);
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
        Assertions.assertIterableEquals(List.of(1, 2, 4), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(future.exceptionNow() instanceof IndexOutOfBoundsException);
    }

    @Test
    public void returnTest() {
        String code = """
                async int func(int param) {
                    try {
                        intStorage.add(1);
                        await futures.create();
                        intStorage.add(2);
                        if (param > 0) {
                            return param * param;
                        }
                        [1][2] = 3; // throws
                        intStorage.add(3);
                    } finally {
                        intStorage.add(4);
                    }
                    return 0;
                }
                intStorage.add(await func(3));
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 4, 9), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void nestedFinallyWithReturnTest() {
        String code = """
                async int func(int param) {
                    try {
                        intStorage.add(1);
                        await futures.create();
                        try {
                            intStorage.add(2);
                            await futures.create();
                            return 2 * param;
                        } finally {
                            intStorage.add(3);
                        }
                    } finally {
                        intStorage.add(4);
                    }
                    return 0;
                }
                try {
                    intStorage.add(await func(3));
                } finally {
                    intStorage.add(10);
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3, 4, 6, 10), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void nestedExceptionTest() {
        String code = """
                try {
                    intStorage.add(1);
                    await futures.create();
                    try {
                        intStorage.add(2);
                        await futures.create();
                        [1][2] = 3; // throws
                    } finally {
                        intStorage.add(3);
                    }
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
        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3, 5), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(future.exceptionNow() instanceof IndexOutOfBoundsException);
    }

    @Test
    public void awaitInFinallyTest() {
        String code = """
                try {
                    intStorage.add(1);
                    await futures.create();
                    intStorage.add(2);
                } finally {
                    intStorage.add(3);
                    await futures.create();
                    intStorage.add(4);
                }
                intStorage.add(5);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3, 4, 5), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void exceptionWithAwaitInFinallyTest() {
        String code = """
                try {
                    intStorage.add(1);
                    await futures.create();
                    [1][2] = 3; // throws
                    intStorage.add(100);
                } finally {
                    intStorage.add(2);
                    await futures.create();
                    intStorage.add(3);
                }
                intStorage.add(4);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(future.exceptionNow() instanceof IndexOutOfBoundsException);
    }

    @Test
    public void exceptionInFinallyOverridesReturnTest() {
        String code = """
                async int func() {
                    try {
                        intStorage.add(1);
                        await futures.create();
                        return 7;
                    } finally {
                        intStorage.add(2);
                        [1][2] = 3; // throws
                    }
                }
                intStorage.add(await func());
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(future.exceptionNow() instanceof IndexOutOfBoundsException);
    }

    @Test
    public void loopContinueBreakBeforeAwaitWithFinallyTest() {
        String code = """
                for (int i = 0; i < 3; i++) {
                    try {
                        intStorage.add(i);
                        if (i == 1) {
                            continue;
                        }
                        if (i == 2) {
                            break;
                        }
                        await futures.create();
                        intStorage.add(10 + i);
                    } finally {
                        intStorage.add(100 + i);
                    }
                }
                intStorage.add(999);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(0), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(0, 10, 100, 1, 101, 2, 102, 999), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void loopContinueBreakAfterAwaitWithFinallyTest() {
        String code = """
                for (int i = 0; i < 3; i++) {
                    try {
                        intStorage.add(i);
                        await futures.create();
                        if (i == 1) {
                            continue;
                        }
                        if (i == 2) {
                            break;
                        }
                        intStorage.add(10 + i);
                    } finally {
                        intStorage.add(100 + i);
                    }
                }
                intStorage.add(999);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(0), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(0, 10, 100, 1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(List.of(0, 10, 100, 1, 101, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(List.of(0, 10, 100, 1, 101, 2, 102, 999), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void catchWithAwaitAndOuterFinallyReturnTest() {
        String code = """
                async int func() {
                    try {
                        try {
                            intStorage.add(1);
                            await futures.create();
                            [1][2] = 3; // throws
                            intStorage.add(100);
                        } catch (e) {
                            intStorage.add(2);
                            await futures.create();
                            return 5;
                        }
                    } finally {
                        intStorage.add(3);
                    }
                    return 0;
                }
                intStorage.add(await func());
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3, 5), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void nestedFinallyExceptionTest() {
        String code = """
                try {
                    try {
                        await futures.create();
                        intStorage.add(1);
                        [1][2] = 3; // throws
                        intStorage.add(999);
                    } finally {
                        intStorage.add(2);
                    }
                } finally {
                    intStorage.add(3);
                }
                intStorage.add(4);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(future.exceptionNow() instanceof IndexOutOfBoundsException);
    }

    @Test
    public void returnFromTryAwaitInFinallyTest() {
        String code = """
                async int func() {
                    try {
                        intStorage.add(1);
                        return 10;
                    } finally {
                        intStorage.add(2);
                        await futures.create();
                        intStorage.add(3);
                    }
                }
                intStorage.add(await func());
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3, 10), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void returnFromTryAwaitWithLoopTest() {
        String code = """
                async int func() {
                    for (int i = 0; i < 3; i++) {
                        try {
                            if (i == 1) return 10;
                            if (i == 2) break;
                            await futures.create();
                        } finally {
                            await futures.create();
                            intStorage.add(100 + i);
                        }
                    }
                    return 20;
                }
                intStorage.add(await func());
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(List.of(100), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(List.of(100, 101, 10), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void innerFinallyThrowsTest() {
        String code = """
                async void func() {
                    try {
                        try {
                            intStorage.add(1);
                        } finally {
                            intStorage.add(2);
                            await futures.create(); // completes exceptionally
                            intStorage.add(3);
                        }
                    } finally {
                        intStorage.add(4);
                    }
                    intStorage.add(999);
                }
                await func();
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).completeExceptionally(new RuntimeException());
        Assertions.assertIterableEquals(List.of(1, 2, 4), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(future.exceptionNow() instanceof RuntimeException);
    }

    @Test
    public void innerTryThrowsTest() {
        String code = """
                async void func() {
                    try {
                        try {
                            intStorage.add(1);
                            [1][2] = 3; // throws
                        } finally {
                            intStorage.add(2);
                            await futures.create(); // completes exceptionally
                        }
                    } finally {
                        intStorage.add(4);
                    }
                }
                await func();
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).completeExceptionally(new RuntimeException());
        Assertions.assertIterableEquals(List.of(1, 2, 4), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(future.exceptionNow() instanceof RuntimeException);
    }

    @Disabled("Limitation of current implementation")
    @Test
    public void loopWithInnerFinallyThrowsTest() {
        String code = """
                async void func() {
                    for (int i = 0; i < 2; i++) {
                        try {
                            try {
                                intStorage.add(i);
                                if (i == 0) break;
                            } finally {
                                intStorage.add(10 + i);
                                await futures.create(); // completes exceptionally
                            }
                        } finally {
                            intStorage.add(20 + i);
                        }
                    }
                }
                await func();
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(0, 10), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).completeExceptionally(new RuntimeException());
        Assertions.assertIterableEquals(List.of(0, 10, 20), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(future.exceptionNow() instanceof RuntimeException);
    }

    public static class ApiRoot {
        public static FutureHelper futures;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}