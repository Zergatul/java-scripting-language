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

public class TryStatementAsyncTests {

    @BeforeEach
    public void clean() {
        ApiRoot.futures = new FutureHelper();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void tryCatchSimpleTest1() {
        String code = """
                try {
                    intStorage.add(1);
                    await futures.create();
                    intStorage.add(2);
                } catch {
                    intStorage.add(3);
                }
                intStorage.add(4);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 4));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void tryCatchSimpleTest2() {
        String code = """
                try {
                    intStorage.add(1);
                    await futures.create();
                    intStorage.add(2);
                    [1][2] = 3; // throws
                    intStorage.add(3);
                } catch {
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
    public void tryCatchInnerBlockTest() {
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
                    }
                    intStorage.add(5);
                } catch {
                    intStorage.add(9999);
                }
                intStorage.add(6);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3, 4, 5, 6));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void tryCatchInnerBlockRethrowTest() {
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
                        throw; // bubble to outer catch
                    }
                    intStorage.add(9999);
                } catch {
                    intStorage.add(5);
                }
                intStorage.add(6);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3, 4, 5, 6));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void tryCatchThrowBeforeAwaitTest() {
        String code = """
                try {
                    intStorage.add(1);
                    [1][2] = 3; // throws before any await
                    await futures.create();
                    intStorage.add(999);
                } catch {
                    intStorage.add(2);
                }
                intStorage.add(3);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3));
        Assertions.assertTrue(future.isDone());
        Assertions.assertEquals(0, ApiRoot.futures.getVoidCount());
    }

    @Test
    public void tryCatchCompleteExceptionallyTest() {
        String code = """
                try {
                    intStorage.add(1);
                    await futures.create(); // will complete exceptionally from the test
                    intStorage.add(999);
                } catch {
                    intStorage.add(2);
                }
                intStorage.add(3);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).completeExceptionally(new RuntimeException("boom"));
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void tryCatchExceptionAfterBlock() {
        String code = """
                try {
                    intStorage.add(1);
                    await futures.create();
                    intStorage.add(2);
                } catch {
                    intStorage.add(3);
                }
                intStorage.add(4);
                [1][2] = 3;
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 4));
        Assertions.assertTrue(future.isDone());

        Assertions.assertTrue(future.exceptionNow() instanceof IndexOutOfBoundsException);
    }

    @Test
    public void tryCatchThrowableVariable() {
        String code = """
                try {
                    intStorage.add(1);
                    await futures.create();
                    intStorage.add(2);
                    [1][2] = 3; // throws
                } catch (e) {
                    intStorage.add(3);
                    stringStorage.add(e.getMessage());
                }
                intStorage.add(4);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3, 4));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("Index 2 out of bounds for length 1"));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void tryCatchCompleteExceptionallyInnerTest() {
        String code = """
                try {
                    intStorage.add(1);
                    try {
                        intStorage.add(2);
                        await futures.create(); // will complete exceptionally
                        intStorage.add(999);
                    } catch {
                        intStorage.add(3);
                    }
                    intStorage.add(4);
                } catch {
                    intStorage.add(9999);
                }
                intStorage.add(5);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).completeExceptionally(new RuntimeException("boom"));
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3, 4, 5));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void tryCatchLoopBreakContinue() {
        // Scenario: A loop where we await, then throw, catch, and continue/break.
        // This tests if the state machine correctly handles control flow jumps from within exception handlers.
        String code = """
                int i = 0;
                while (i < 5) {
                    try {
                        intStorage.add(10 + i);
                        await futures.create(); // Suspend here
                        if (i == 2) {
                             [1][2] = 3; // Throw on index 2
                        }
                        intStorage.add(20 + i);
                    } catch {
                        intStorage.add(30 + i);
                        i++;
                        continue; // Jump back to loop start
                    }
                    i++;
                }
                intStorage.add(99);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        // Iteration 0
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10));
        ApiRoot.futures.get(0).complete(null);
        // 10 -> await -> 20 (success) -> loop inc

        // Iteration 1
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 20, 11));
        ApiRoot.futures.get(1).complete(null);
        // 11 -> await -> 21 (success) -> loop inc

        // Iteration 2 (The Exception)
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 20, 11, 21, 12));
        ApiRoot.futures.get(2).complete(null);
        // 12 -> await -> THROW -> catch(32) -> continue

        // Iteration 3
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 20, 11, 21, 12, 32, 13));
        ApiRoot.futures.get(3).complete(null);

        // Iteration 4
        ApiRoot.futures.get(4).complete(null);

        // Final check
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(
                10, 20, // i=0
                11, 21, // i=1
                12, 32, // i=2 (exception caught, 22 skipped)
                13, 23, // i=3
                14, 24, // i=4
                99      // End
        ));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void tryCatchReturnFromCatch() {
        String code = """
                async int test() {
                    try {
                        await futures.create();
                        throw new Java<java.lang.RuntimeException>();
                    } catch {
                        intStorage.add(2);
                        return 999;
                    }
                    return 0;
                }
                
                intStorage.add(1);
                int result = await test();
                intStorage.add(result);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));

        // Complete inner future
        ApiRoot.futures.get(0).complete(null);

        // Should have hit catch, added 2, and returned 999
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 999));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void tryCatchMultipleAwaits() {
        String code = """
                try {
                    intStorage.add(1);
                    await futures.create();
                    intStorage.add(2);
                    if (await futures.createBool()) {
                         throw new Java<java.lang.RuntimeException>();
                    }
                    await futures.create();
                    intStorage.add(3);
                } catch {
                    intStorage.add(4);
                }
                intStorage.add(5);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);

        // pass 1
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(true);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 4, 5));
        Assertions.assertTrue(future.isDone());

        // pass 2
        clean();

        future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3, 5));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void tryCatchExceptionInAwaitArguments() {
        String code = """
                async void risky(int x) => await futures.create();
                int thrower() => throw new Java<java.lang.RuntimeException>();
                
                try {
                    intStorage.add(1);
                    // The exception happens whilst evaluating arguments for the async call.
                    // The async method 'risky' should not even start.
                    await risky(thrower());
                    intStorage.add(2);
                } catch {
                    intStorage.add(3);
                }
                intStorage.add(4);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        // No futures should be created because 'risky' wasn't called successfully
        Assertions.assertEquals(0, ApiRoot.futures.getVoidCount());

        // 1 -> thrower() -> catch(3) -> 4
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 3, 4));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void tryCatchAwaitInsideCatchThenThrow() {
        String code = """
                try {
                    await futures.create(); // 1. Suspend
                    throw new Java<java.lang.RuntimeException>(); // 2. Throw
                } catch {
                    intStorage.add(1);
                    await futures.create(); // 3. Suspend inside catch
                    intStorage.add(2);
                    throw; // 4. Rethrow original or new exception
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        // 1. Resume main try
        ApiRoot.futures.get(0).complete(null);

        // Should be in catch now
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));

        // 2. Resume catch
        ApiRoot.futures.get(1).complete(null);

        // Should have added 2, then thrown. Future should fail.
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2));
        Assertions.assertTrue(future.exceptionNow() instanceof RuntimeException);
    }

    public static class ApiRoot {
        public static FutureHelper futures;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}