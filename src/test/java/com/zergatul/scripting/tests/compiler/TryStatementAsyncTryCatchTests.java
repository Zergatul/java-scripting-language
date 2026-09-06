package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.ObjectStorage;
import com.zergatul.scripting.utility.Lists;

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
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getExceptionNow;

public class TryStatementAsyncTryCatchTests {

    @BeforeEach
    public void clean() {
        ApiRoot.futures = new FutureHelper();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.objectStorage = new ObjectStorage();
    }

    @Test
    public void syncTryStatementTest() {
        String code =
                "try {\n" +
                "    [1][2] = 3;\n" +
                "} catch (e) {\n" +
                "    objectStorage.add(e);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertEquals(1, ApiRoot.objectStorage.list.size());
        Assertions.assertTrue(ApiRoot.objectStorage.list.get(0) instanceof ArrayIndexOutOfBoundsException);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void simpleTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(2);\n" +
                "} catch {\n" +
                "    intStorage.add(3);\n" +
                "}\n" +
                "intStorage.add(4);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 4));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void exceptionTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(2);\n" +
                "    [1][2] = 3; // throws\n" +
                "    intStorage.add(3);\n" +
                "} catch {\n" +
                "    intStorage.add(4);\n" +
                "}\n" +
                "intStorage.add(5);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 4, 5));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void innerBlockTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(2);\n" +
                "    try {\n" +
                "        intStorage.add(3);\n" +
                "        [1][2] = 3; // throws\n" +
                "        intStorage.add(999);\n" +
                "    } catch {\n" +
                "        intStorage.add(4);\n" +
                "    }\n" +
                "    intStorage.add(5);\n" +
                "} catch {\n" +
                "    intStorage.add(9999);\n" +
                "}\n" +
                "intStorage.add(6);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 3, 4, 5, 6));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void innerBlockRethrowTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(2);\n" +
                "    try {\n" +
                "        intStorage.add(3);\n" +
                "        [1][2] = 3; // throws\n" +
                "        intStorage.add(999);\n" +
                "    } catch {\n" +
                "        intStorage.add(4);\n" +
                "        throw; // bubble to outer catch\n" +
                "    }\n" +
                "    intStorage.add(9999);\n" +
                "} catch {\n" +
                "    intStorage.add(5);\n" +
                "}\n" +
                "intStorage.add(6);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 3, 4, 5, 6));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void throwBeforeAwaitTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    [1][2] = 3; // throws before any await\n" +
                "    await futures.create();\n" +
                "    intStorage.add(999);\n" +
                "} catch {\n" +
                "    intStorage.add(2);\n" +
                "}\n" +
                "intStorage.add(3);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 3));
        Assertions.assertTrue(future.isDone());
        Assertions.assertEquals(0, ApiRoot.futures.getVoidCount());
    }

    @Test
    public void completeExceptionallyTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create(); // will complete exceptionally from the test\n" +
                "    intStorage.add(999);\n" +
                "} catch {\n" +
                "    intStorage.add(2);\n" +
                "}\n" +
                "intStorage.add(3);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).completeExceptionally(new RuntimeException("boom"));
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 3));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void exceptionAfterBlockTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(2);\n" +
                "} catch {\n" +
                "    intStorage.add(3);\n" +
                "}\n" +
                "intStorage.add(4);\n" +
                "[1][2] = 3;\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 4));
        Assertions.assertTrue(future.isDone());

        Assertions.assertTrue(getExceptionNow(future) instanceof IndexOutOfBoundsException);
    }

    @Test
    public void throwableVariableTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(2);\n" +
                "    [1][2] = 3; // throws\n" +
                "} catch (e) {\n" +
                "    intStorage.add(3);\n" +
                "    objectStorage.add(e);\n" +
                "}\n" +
                "intStorage.add(4);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 3, 4));
        Assertions.assertEquals(1, ApiRoot.objectStorage.list.size());
        Assertions.assertTrue(ApiRoot.objectStorage.list.get(0) instanceof ArrayIndexOutOfBoundsException);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void completeExceptionallyInnerTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    try {\n" +
                "        intStorage.add(2);\n" +
                "        await futures.create(); // will complete exceptionally\n" +
                "        intStorage.add(999);\n" +
                "    } catch {\n" +
                "        intStorage.add(3);\n" +
                "    }\n" +
                "    intStorage.add(4);\n" +
                "} catch {\n" +
                "    intStorage.add(9999);\n" +
                "}\n" +
                "intStorage.add(5);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).completeExceptionally(new RuntimeException("boom"));
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 3, 4, 5));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void loopBreakContinueTest() {
        // Scenario: A loop where we await, then throw, catch, and continue/break.
        // This tests if the state machine correctly handles control flow jumps from within exception handlers.
        String code =
                "int i = 0;\n" +
                "while (i < 5) {\n" +
                "    try {\n" +
                "        intStorage.add(10 + i);\n" +
                "        await futures.create(); // Suspend here\n" +
                "        if (i == 2) {\n" +
                "             [1][2] = 3; // Throw on index 2\n" +
                "        }\n" +
                "        intStorage.add(20 + i);\n" +
                "    } catch {\n" +
                "        intStorage.add(30 + i);\n" +
                "        i++;\n" +
                "        continue; // Jump back to loop start\n" +
                "    }\n" +
                "    i++;\n" +
                "}\n" +
                "intStorage.add(99);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        // Iteration 0
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10));
        ApiRoot.futures.get(0).complete(null);
        // 10 -> await -> 20 (success) -> loop inc

        // Iteration 1
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 20, 11));
        ApiRoot.futures.get(1).complete(null);
        // 11 -> await -> 21 (success) -> loop inc

        // Iteration 2 (The Exception)
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 20, 11, 21, 12));
        ApiRoot.futures.get(2).complete(null);
        // 12 -> await -> THROW -> catch(32) -> continue

        // Iteration 3
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 20, 11, 21, 12, 32, 13));
        ApiRoot.futures.get(3).complete(null);

        // Iteration 4
        ApiRoot.futures.get(4).complete(null);

        // Final check
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(
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
    public void returnFromCatchTest() {
        String code =
                "async int test() {\n" +
                "    try {\n" +
                "        await futures.create();\n" +
                "        throw new Java<java.lang.RuntimeException>();\n" +
                "    } catch {\n" +
                "        intStorage.add(2);\n" +
                "        return 999;\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(1);\n" +
                "int result = await test();\n" +
                "intStorage.add(result);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));

        // Complete inner future
        ApiRoot.futures.get(0).complete(null);

        // Should have hit catch, added 2, and returned 999
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 999));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void multipleAwaitsTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(2);\n" +
                "    if (await futures.createBool()) {\n" +
                "         throw new Java<java.lang.RuntimeException>();\n" +
                "    }\n" +
                "    await futures.create();\n" +
                "    intStorage.add(3);\n" +
                "} catch {\n" +
                "    intStorage.add(4);\n" +
                "}\n" +
                "intStorage.add(5);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);

        // pass 1
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(true);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 4, 5));
        Assertions.assertTrue(future.isDone());

        // pass 2
        clean();

        future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 3, 5));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void exceptionInAwaitArgumentsTest() {
        String code =
                "async void risky(int x) => await futures.create();\n" +
                "int thrower() => throw new Java<java.lang.RuntimeException>();\n" +
                "\n" +
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    // The exception happens whilst evaluating arguments for the async call.\n" +
                "    // The async method 'risky' should not even start.\n" +
                "    await risky(thrower());\n" +
                "    intStorage.add(2);\n" +
                "} catch {\n" +
                "    intStorage.add(3);\n" +
                "}\n" +
                "intStorage.add(4);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        // No futures should be created because 'risky' wasn't called successfully
        Assertions.assertEquals(0, ApiRoot.futures.getVoidCount());

        // 1 -> thrower() -> catch(3) -> 4
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 3, 4));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void awaitInsideCatchThenThrowTest() {
        String code =
                "try {\n" +
                "    await futures.create(); // 1. Suspend\n" +
                "    throw new Java<java.lang.RuntimeException>(); // 2. Throw\n" +
                "} catch {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create(); // 3. Suspend inside catch\n" +
                "    intStorage.add(2);\n" +
                "    throw; // 4. Rethrow original or new exception\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        // 1. Resume main try
        ApiRoot.futures.get(0).complete(null);

        // Should be in catch now
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));

        // 2. Resume catch
        ApiRoot.futures.get(1).complete(null);

        // Should have added 2, then thrown. Future should fail.
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2));
        Assertions.assertTrue(getExceptionNow(future) instanceof RuntimeException);
    }

    public static class ApiRoot {
        public static FutureHelper futures;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static ObjectStorage objectStorage;
    }
}