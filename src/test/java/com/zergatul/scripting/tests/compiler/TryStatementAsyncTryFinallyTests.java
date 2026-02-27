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
    public void nestedFinallyReturnFromTryBlockTest() {
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
    public void nestedFinallyReturnFromFinallyBlockTest() {
        String code = """
                async int func(int param) {
                    try {
                        intStorage.add(1);
                        await futures.create();
                        try {
                            intStorage.add(2);
                            await futures.create();
                            intStorage.add(3);
                        } finally {
                            intStorage.add(4);
                            await futures.create();
                            if (param > 0) {
                                return 2 * param;
                            }
                        }
                    } finally {
                        intStorage.add(5);
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
        Assertions.assertIterableEquals(List.of(1, 2, 3, 4), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3, 4, 5, 6, 10), ApiRoot.intStorage.list);
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

    @Test
    public void returnInFinallyOverridesExceptionTest() {
        String code = """
                async int func() {
                    try {
                        intStorage.add(1);
                        await futures.create();
                        intStorage.add(2);
                        [1][2] = 3; // throws
                        intStorage.add(999);
                    } finally {
                        intStorage.add(3);
                        await futures.create();
                        intStorage.add(4);
                        return 7; // should override the exception from try
                    }
                }
                intStorage.add(await func());
                intStorage.add(8);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3, 4, 7, 8), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void innerFinallyReturnsOuterFinallyThrowsTest() {
        String code = """
                async int func() {
                    try {
                        try {
                            intStorage.add(1);
                            await futures.create();      // F0
                            [1][2] = 3;                  // throws
                            intStorage.add(999);
                        } finally {
                            intStorage.add(2);
                            await futures.create();      // F1
                            intStorage.add(3);
                            return 10;                   // overrides inner exception
                        }
                    } finally {
                        intStorage.add(4);
                        await futures.create();          // F2
                        intStorage.add(5);
                        [1][2] = 3;                      // throws, overrides pending return=10
                    }
                }
                
                try {
                    intStorage.add(await func());
                } catch (e) {
                    intStorage.add(777);                // should execute (outer finally throws)
                }
                intStorage.add(888);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3, 4), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3, 4, 5, 777, 888), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void loopBreakContinueInFinallyTest() {
        String code = """
                for (int i = 0; i < 3; i++) {
                    try {
                        intStorage.add(i);
                        await futures.create();            // F0, F2, ...
                        intStorage.add(10 + i);
                    } finally {
                        intStorage.add(100 + i);
                        await futures.create();            // F1, F3, ...
                        if (i == 0) {
                            continue;
                        }
                        if (i == 1) {
                            break;
                        }
                        intStorage.add(9999);
                    }
                    intStorage.add(200 + i);
                }
                intStorage.add(7777);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(0), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(0, 10, 100), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(List.of(0, 10, 100, 1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(List.of(0, 10, 100, 1, 11, 101), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(3).complete(null);
        Assertions.assertIterableEquals(List.of(0, 10, 100, 1, 11, 101, 7777), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void exceptionInTryInLoopTest() {
        String code = """
                async void test() {
                    try {
                        for (int i = 0; i < 3; i++) {
                            try {
                                if (i == -1) continue;
                                if (i == -2) break;
                                intStorage.add(10 + i);
                                await futures.create();           // F0
                                if (i == 0) {
                                    [1][2] = 3;                   // throws
                                }
                                intStorage.add(1000 + i);
                            } finally {
                                intStorage.add(20 + i);
                                await futures.create();           // F1
                                intStorage.add(30 + i);
                            }
                            intStorage.add(40 + i);
                        }
                        intStorage.add(9999);
                    } finally {
                        intStorage.add(500);
                        await futures.create();                   // F2
                        intStorage.add(600);
                    }
                }
                try {
                    await test();
                } catch (e) {
                    intStorage.add(700);
                }
                intStorage.add(800);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(10), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(10, 20), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(List.of(10, 20, 30, 500), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(List.of(10, 20, 30, 500, 600, 700, 800), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void exceptionInFinallyInLoopBreakTest() {
        String code = """
                async void test() {
                    try {
                        for (int i = 0; i < 3; i++) {
                            try {
                                intStorage.add(i);
                                if (i == 0) {
                                    continue;
                                }
                                if (i == 1) {
                                    break;
                                }
                                intStorage.add(1000 + i);
                            } finally {
                                intStorage.add(10 + i);
                                await futures.create();       // F0, F1 (completes exceptionally)
                                intStorage.add(20 + i);
                            }
                            intStorage.add(2000 + i);
                        }
                        intStorage.add(9999);
                    } finally {
                        intStorage.add(500);
                    }
                }
                try {
                    await test();
                } catch {
                    intStorage.add(700);
                }
                intStorage.add(800);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(0, 10), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(0, 10, 20, 1, 11), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).completeExceptionally(new RuntimeException());
        Assertions.assertIterableEquals(List.of(0, 10, 20, 1, 11, 500, 700, 800), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void exceptionInFinallyInLoopContinueTest() {
        String code = """
                async void test() {
                    try {
                        for (int i = 0; i < 3; i++) {
                            try {
                                intStorage.add(i);
                                if (i == 0) {
                                    continue;
                                }
                                if (i == 1) {
                                    break;
                                }
                                intStorage.add(1000 + i);
                            } finally {
                                intStorage.add(10 + i);
                                await futures.create();       // F0 (completes exceptionally)
                                intStorage.add(20 + i);
                            }
                            intStorage.add(2000 + i);
                        }
                        intStorage.add(9999);
                    } finally {
                        intStorage.add(500);
                    }
                }
                try {
                    await test();
                } catch {
                    intStorage.add(700);
                }
                intStorage.add(800);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(0, 10), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).completeExceptionally(new RuntimeException());
        Assertions.assertIterableEquals(List.of(0, 10, 500, 700, 800), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void loopJumpOverrideInFinallyTest() {
        String code = """
                for (int i = 0; i < 3; i++) {
                    try {
                        intStorage.add(i);
                        if (i == 0) {
                            continue;
                        }
                        intStorage.add(10 + i);
                        await futures.create();
                        intStorage.add(20 + i);
                    } finally {
                        intStorage.add(100 + i);
                        await futures.create();           // F0
                        if (i == 0) {
                            break;
                        }
                        intStorage.add(200 + i);
                    }
                    intStorage.add(300 + i);
                }
                intStorage.add(9999);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(0, 100), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(0, 100, 9999), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void pendingJumpOverrideByReturnTest() {
        String code = """
                async int func() {
                    for (int i = 0; i < 2; i++) {
                        try {
                            intStorage.add(i);
                            if (i == 0) {
                                continue;
                            }
                            intStorage.add(999);
                        } finally {
                            intStorage.add(10 + i);
                            await futures.create();       // F0
                            intStorage.add(20 + i);
                            return 42;
                        }
                        intStorage.add(1000 + i);
                    }
                    return 123;
                }
                intStorage.add(await func());
                intStorage.add(7777);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(0, 10), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(0, 10, 20, 42, 7777), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void pendingJumpOverrideByReturnAndOuterFinallyTest() {
        String code = """
                async int func() {
                    try {
                        for (int i = 0; i < 1; i++) {
                            try {
                                intStorage.add(1);
                                continue;
                            } finally {
                                intStorage.add(2);
                                await futures.create();   // F0
                                intStorage.add(3);
                                return 9;
                            }
                        }
                        intStorage.add(999);
                    } finally {
                        intStorage.add(4);
                        await futures.create();           // F1
                        intStorage.add(5);
                    }
                    return 0;
                }
                intStorage.add(await func());
                intStorage.add(777);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(List.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3, 4), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(List.of(1, 2, 3, 4, 5, 9, 777), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    public static class ApiRoot {
        public static FutureHelper futures;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}