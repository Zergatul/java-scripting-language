package com.zergatul.scripting.tests.compiler;

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

public class TryStatementAsyncTryFinallyTests {

    @BeforeEach
    public void clean() {
        ApiRoot.futures = new FutureHelper();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void noExceptionTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(2);\n" +
                "} finally {\n" +
                "    intStorage.add(3);\n" +
                "}\n" +
                "intStorage.add(4);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4), ApiRoot.intStorage.list);
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
                "} finally {\n" +
                "    intStorage.add(4);\n" +
                "}\n" +
                "intStorage.add(5);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 4), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(getExceptionNow(future) instanceof IndexOutOfBoundsException);
    }

    @Test
    public void returnTest() {
        String code =
                "async int func(int param) {\n" +
                "    try {\n" +
                "        intStorage.add(1);\n" +
                "        await futures.create();\n" +
                "        intStorage.add(2);\n" +
                "        if (param > 0) {\n" +
                "            return param * param;\n" +
                "        }\n" +
                "        [1][2] = 3; // throws\n" +
                "        intStorage.add(3);\n" +
                "    } finally {\n" +
                "        intStorage.add(4);\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n" +
                "intStorage.add(await func(3));\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 4, 9), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void nestedFinallyReturnFromTryBlockTest() {
        String code =
                "async int func(int param) {\n" +
                "    try {\n" +
                "        intStorage.add(1);\n" +
                "        await futures.create();\n" +
                "        try {\n" +
                "            intStorage.add(2);\n" +
                "            await futures.create();\n" +
                "            return 2 * param;\n" +
                "        } finally {\n" +
                "            intStorage.add(3);\n" +
                "        }\n" +
                "    } finally {\n" +
                "        intStorage.add(4);\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n" +
                "try {\n" +
                "    intStorage.add(await func(3));\n" +
                "} finally {\n" +
                "    intStorage.add(10);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 6, 10), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void nestedFinallyReturnFromFinallyBlockTest() {
        String code =
                "async int func(int param) {\n" +
                "    try {\n" +
                "        intStorage.add(1);\n" +
                "        await futures.create();\n" +
                "        try {\n" +
                "            intStorage.add(2);\n" +
                "            await futures.create();\n" +
                "            intStorage.add(3);\n" +
                "        } finally {\n" +
                "            intStorage.add(4);\n" +
                "            await futures.create();\n" +
                "            if (param > 0) {\n" +
                "                return 2 * param;\n" +
                "            }\n" +
                "        }\n" +
                "    } finally {\n" +
                "        intStorage.add(5);\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n" +
                "try {\n" +
                "    intStorage.add(await func(3));\n" +
                "} finally {\n" +
                "    intStorage.add(10);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5, 6, 10), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void nestedExceptionTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create();\n" +
                "    try {\n" +
                "        intStorage.add(2);\n" +
                "        await futures.create();\n" +
                "        [1][2] = 3; // throws\n" +
                "    } finally {\n" +
                "        intStorage.add(3);\n" +
                "    }\n" +
                "    intStorage.add(4);\n" +
                "} finally {\n" +
                "    intStorage.add(5);\n" +
                "}\n" +
                "intStorage.add(6);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 5), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(getExceptionNow(future) instanceof IndexOutOfBoundsException);
    }

    @Test
    public void awaitInFinallyTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(2);\n" +
                "} finally {\n" +
                "    intStorage.add(3);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(4);\n" +
                "}\n" +
                "intStorage.add(5);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void exceptionWithAwaitInFinallyTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create();\n" +
                "    [1][2] = 3; // throws\n" +
                "    intStorage.add(100);\n" +
                "} finally {\n" +
                "    intStorage.add(2);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(3);\n" +
                "}\n" +
                "intStorage.add(4);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(getExceptionNow(future) instanceof IndexOutOfBoundsException);
    }

    @Test
    public void exceptionInFinallyOverridesReturnTest() {
        String code =
                "async int func() {\n" +
                "    try {\n" +
                "        intStorage.add(1);\n" +
                "        await futures.create();\n" +
                "        return 7;\n" +
                "    } finally {\n" +
                "        intStorage.add(2);\n" +
                "        [1][2] = 3; // throws\n" +
                "    }\n" +
                "}\n" +
                "intStorage.add(await func());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(getExceptionNow(future) instanceof IndexOutOfBoundsException);
    }

    @Test
    public void loopContinueBreakBeforeAwaitWithFinallyTest() {
        String code =
                "for (int i = 0; i < 3; i++) {\n" +
                "    try {\n" +
                "        intStorage.add(i);\n" +
                "        if (i == 1) {\n" +
                "            continue;\n" +
                "        }\n" +
                "        if (i == 2) {\n" +
                "            break;\n" +
                "        }\n" +
                "        await futures.create();\n" +
                "        intStorage.add(10 + i);\n" +
                "    } finally {\n" +
                "        intStorage.add(100 + i);\n" +
                "    }\n" +
                "}\n" +
                "intStorage.add(999);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(0), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100, 1, 101, 2, 102, 999), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void loopContinueBreakAfterAwaitWithFinallyTest() {
        String code =
                "for (int i = 0; i < 3; i++) {\n" +
                "    try {\n" +
                "        intStorage.add(i);\n" +
                "        await futures.create();\n" +
                "        if (i == 1) {\n" +
                "            continue;\n" +
                "        }\n" +
                "        if (i == 2) {\n" +
                "            break;\n" +
                "        }\n" +
                "        intStorage.add(10 + i);\n" +
                "    } finally {\n" +
                "        intStorage.add(100 + i);\n" +
                "    }\n" +
                "}\n" +
                "intStorage.add(999);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(0), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100, 1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100, 1, 101, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100, 1, 101, 2, 102, 999), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void catchWithAwaitAndOuterFinallyReturnTest() {
        String code =
                "async int func() {\n" +
                "    try {\n" +
                "        try {\n" +
                "            intStorage.add(1);\n" +
                "            await futures.create();\n" +
                "            [1][2] = 3; // throws\n" +
                "            intStorage.add(100);\n" +
                "        } catch (e) {\n" +
                "            intStorage.add(2);\n" +
                "            await futures.create();\n" +
                "            return 5;\n" +
                "        }\n" +
                "    } finally {\n" +
                "        intStorage.add(3);\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n" +
                "intStorage.add(await func());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 5), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void nestedFinallyExceptionTest() {
        String code =
                "try {\n" +
                "    try {\n" +
                "        await futures.create();\n" +
                "        intStorage.add(1);\n" +
                "        [1][2] = 3; // throws\n" +
                "        intStorage.add(999);\n" +
                "    } finally {\n" +
                "        intStorage.add(2);\n" +
                "    }\n" +
                "} finally {\n" +
                "    intStorage.add(3);\n" +
                "}\n" +
                "intStorage.add(4);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(getExceptionNow(future) instanceof IndexOutOfBoundsException);
    }

    @Test
    public void returnFromTryAwaitInFinallyTest() {
        String code =
                "async int func() {\n" +
                "    try {\n" +
                "        intStorage.add(1);\n" +
                "        return 10;\n" +
                "    } finally {\n" +
                "        intStorage.add(2);\n" +
                "        await futures.create();\n" +
                "        intStorage.add(3);\n" +
                "    }\n" +
                "}\n" +
                "intStorage.add(await func());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 10), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void returnFromTryAwaitWithLoopTest() {
        String code =
                "async int func() {\n" +
                "    for (int i = 0; i < 3; i++) {\n" +
                "        try {\n" +
                "            if (i == 1) return 10;\n" +
                "            await futures.create();\n" +
                "        } finally {\n" +
                "            await futures.create();\n" +
                "            intStorage.add(100 + i);\n" +
                "        }\n" +
                "    }\n" +
                "    return 20;\n" +
                "}\n" +
                "intStorage.add(await func());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(100), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(Lists.of(100, 101, 10), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void innerFinallyThrowsTest() {
        String code =
                "async void func() {\n" +
                "    try {\n" +
                "        try {\n" +
                "            intStorage.add(1);\n" +
                "        } finally {\n" +
                "            intStorage.add(2);\n" +
                "            await futures.create(); // completes exceptionally\n" +
                "            intStorage.add(3);\n" +
                "        }\n" +
                "    } finally {\n" +
                "        intStorage.add(4);\n" +
                "    }\n" +
                "    intStorage.add(999);\n" +
                "}\n" +
                "await func();\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).completeExceptionally(new RuntimeException());
        Assertions.assertIterableEquals(Lists.of(1, 2, 4), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(getExceptionNow(future) instanceof RuntimeException);
    }

    @Test
    public void innerTryThrowsTest() {
        String code =
                "async void func() {\n" +
                "    try {\n" +
                "        try {\n" +
                "            intStorage.add(1);\n" +
                "            [1][2] = 3; // throws\n" +
                "        } finally {\n" +
                "            intStorage.add(2);\n" +
                "            await futures.create(); // completes exceptionally\n" +
                "        }\n" +
                "    } finally {\n" +
                "        intStorage.add(4);\n" +
                "    }\n" +
                "}\n" +
                "await func();\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).completeExceptionally(new RuntimeException());
        Assertions.assertIterableEquals(Lists.of(1, 2, 4), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(getExceptionNow(future) instanceof RuntimeException);
    }

    @Test
    public void loopWithInnerFinallyThrowsTest() {
        String code =
                "async void func() {\n" +
                "    for (int i = 0; i < 2; i++) {\n" +
                "        try {\n" +
                "            try {\n" +
                "                intStorage.add(i);\n" +
                "                if (i == 0) break;\n" +
                "            } finally {\n" +
                "                intStorage.add(10 + i);\n" +
                "                await futures.create(); // completes exceptionally\n" +
                "            }\n" +
                "        } finally {\n" +
                "            intStorage.add(20 + i);\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "await func();\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(0, 10), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).completeExceptionally(new RuntimeException());
        Assertions.assertIterableEquals(Lists.of(0, 10, 20), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
        Assertions.assertTrue(getExceptionNow(future) instanceof RuntimeException);
    }

    @Test
    public void returnInFinallyOverridesExceptionTest() {
        String code =
                "async int func() {\n" +
                "    try {\n" +
                "        intStorage.add(1);\n" +
                "        await futures.create();\n" +
                "        intStorage.add(2);\n" +
                "        [1][2] = 3; // throws\n" +
                "        intStorage.add(999);\n" +
                "    } finally {\n" +
                "        intStorage.add(3);\n" +
                "        await futures.create();\n" +
                "        intStorage.add(4);\n" +
                "        return 7; // should override the exception from try\n" +
                "    }\n" +
                "}\n" +
                "intStorage.add(await func());\n" +
                "intStorage.add(8);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 7, 8), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void innerFinallyReturnsOuterFinallyThrowsTest() {
        String code =
                "async int func() {\n" +
                "    try {\n" +
                "        try {\n" +
                "            intStorage.add(1);\n" +
                "            await futures.create();      // F0\n" +
                "            [1][2] = 3;                  // throws\n" +
                "            intStorage.add(999);\n" +
                "        } finally {\n" +
                "            intStorage.add(2);\n" +
                "            await futures.create();      // F1\n" +
                "            intStorage.add(3);\n" +
                "            return 10;                   // overrides inner exception\n" +
                "        }\n" +
                "    } finally {\n" +
                "        intStorage.add(4);\n" +
                "        await futures.create();          // F2\n" +
                "        intStorage.add(5);\n" +
                "        [1][2] = 3;                      // throws, overrides pending return=10\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "try {\n" +
                "    intStorage.add(await func());\n" +
                "} catch (e) {\n" +
                "    intStorage.add(777);                // should execute (outer finally throws)\n" +
                "}\n" +
                "intStorage.add(888);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5, 777, 888), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void loopBreakContinueInFinallyTest() {
        String code =
                "for (int i = 0; i < 3; i++) {\n" +
                "    try {\n" +
                "        intStorage.add(i);\n" +
                "        await futures.create();            // F0, F2, ...\n" +
                "        intStorage.add(10 + i);\n" +
                "    } finally {\n" +
                "        intStorage.add(100 + i);\n" +
                "        await futures.create();            // F1, F3, ...\n" +
                "        if (i == 0) {\n" +
                "            continue;\n" +
                "        }\n" +
                "        if (i == 1) {\n" +
                "            break;\n" +
                "        }\n" +
                "        intStorage.add(9999);\n" +
                "    }\n" +
                "    intStorage.add(200 + i);\n" +
                "}\n" +
                "intStorage.add(7777);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(0), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100, 1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100, 1, 11, 101), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(3).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100, 1, 11, 101, 7777), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void exceptionInTryInLoopTest() {
        String code =
                "async void test() {\n" +
                "    try {\n" +
                "        for (int i = 0; i < 3; i++) {\n" +
                "            try {\n" +
                "                if (i == -1) continue;\n" +
                "                if (i == -2) break;\n" +
                "                intStorage.add(10 + i);\n" +
                "                await futures.create();           // F0\n" +
                "                if (i == 0) {\n" +
                "                    [1][2] = 3;                   // throws\n" +
                "                }\n" +
                "                intStorage.add(1000 + i);\n" +
                "            } finally {\n" +
                "                intStorage.add(20 + i);\n" +
                "                await futures.create();           // F1\n" +
                "                intStorage.add(30 + i);\n" +
                "            }\n" +
                "            intStorage.add(40 + i);\n" +
                "        }\n" +
                "        intStorage.add(9999);\n" +
                "    } finally {\n" +
                "        intStorage.add(500);\n" +
                "        await futures.create();                   // F2\n" +
                "        intStorage.add(600);\n" +
                "    }\n" +
                "}\n" +
                "try {\n" +
                "    await test();\n" +
                "} catch (e) {\n" +
                "    intStorage.add(700);\n" +
                "}\n" +
                "intStorage.add(800);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(10), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(10, 20), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(10, 20, 30, 500), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(Lists.of(10, 20, 30, 500, 600, 700, 800), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void exceptionInFinallyInLoopBreakTest() {
        String code =
                "async void test() {\n" +
                "    try {\n" +
                "        for (int i = 0; i < 3; i++) {\n" +
                "            try {\n" +
                "                intStorage.add(i);\n" +
                "                if (i == 0) {\n" +
                "                    continue;\n" +
                "                }\n" +
                "                if (i == 1) {\n" +
                "                    break;\n" +
                "                }\n" +
                "                intStorage.add(1000 + i);\n" +
                "            } finally {\n" +
                "                intStorage.add(10 + i);\n" +
                "                await futures.create();       // F0, F1 (completes exceptionally)\n" +
                "                intStorage.add(20 + i);\n" +
                "            }\n" +
                "            intStorage.add(2000 + i);\n" +
                "        }\n" +
                "        intStorage.add(9999);\n" +
                "    } finally {\n" +
                "        intStorage.add(500);\n" +
                "    }\n" +
                "}\n" +
                "try {\n" +
                "    await test();\n" +
                "} catch {\n" +
                "    intStorage.add(700);\n" +
                "}\n" +
                "intStorage.add(800);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(0, 10), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 20, 1, 11), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).completeExceptionally(new RuntimeException());
        Assertions.assertIterableEquals(Lists.of(0, 10, 20, 1, 11, 500, 700, 800), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void exceptionInFinallyInLoopContinueTest() {
        String code =
                "async void test() {\n" +
                "    try {\n" +
                "        for (int i = 0; i < 3; i++) {\n" +
                "            try {\n" +
                "                intStorage.add(i);\n" +
                "                if (i == 0) {\n" +
                "                    continue;\n" +
                "                }\n" +
                "                if (i == 1) {\n" +
                "                    break;\n" +
                "                }\n" +
                "                intStorage.add(1000 + i);\n" +
                "            } finally {\n" +
                "                intStorage.add(10 + i);\n" +
                "                await futures.create();       // F0 (completes exceptionally)\n" +
                "                intStorage.add(20 + i);\n" +
                "            }\n" +
                "            intStorage.add(2000 + i);\n" +
                "        }\n" +
                "        intStorage.add(9999);\n" +
                "    } finally {\n" +
                "        intStorage.add(500);\n" +
                "    }\n" +
                "}\n" +
                "try {\n" +
                "    await test();\n" +
                "} catch {\n" +
                "    intStorage.add(700);\n" +
                "}\n" +
                "intStorage.add(800);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(0, 10), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).completeExceptionally(new RuntimeException());
        Assertions.assertIterableEquals(Lists.of(0, 10, 500, 700, 800), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void loopJumpOverrideInFinallyTest() {
        String code =
                "for (int i = 0; i < 3; i++) {\n" +
                "    try {\n" +
                "        intStorage.add(i);\n" +
                "        if (i == 0) {\n" +
                "            continue;\n" +
                "        }\n" +
                "        intStorage.add(10 + i);\n" +
                "        await futures.create();\n" +
                "        intStorage.add(20 + i);\n" +
                "    } finally {\n" +
                "        intStorage.add(100 + i);\n" +
                "        await futures.create();           // F0\n" +
                "        if (i == 0) {\n" +
                "            break;\n" +
                "        }\n" +
                "        intStorage.add(200 + i);\n" +
                "    }\n" +
                "    intStorage.add(300 + i);\n" +
                "}\n" +
                "intStorage.add(9999);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(0, 100), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 100, 9999), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void pendingJumpOverrideByReturnTest() {
        String code =
                "async int func() {\n" +
                "    for (int i = 0; i < 2; i++) {\n" +
                "        try {\n" +
                "            intStorage.add(i);\n" +
                "            if (i == 0) {\n" +
                "                continue;\n" +
                "            }\n" +
                "            intStorage.add(999);\n" +
                "        } finally {\n" +
                "            intStorage.add(10 + i);\n" +
                "            await futures.create();       // F0\n" +
                "            intStorage.add(20 + i);\n" +
                "            return 42;\n" +
                "        }\n" +
                "        intStorage.add(1000 + i);\n" +
                "    }\n" +
                "    return 123;\n" +
                "}\n" +
                "intStorage.add(await func());\n" +
                "intStorage.add(7777);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(0, 10), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 20, 42, 7777), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void pendingJumpOverrideByReturnAndOuterFinallyTest() {
        String code =
                "async int func() {\n" +
                "    try {\n" +
                "        for (int i = 0; i < 1; i++) {\n" +
                "            try {\n" +
                "                intStorage.add(1);\n" +
                "                continue;\n" +
                "            } finally {\n" +
                "                intStorage.add(2);\n" +
                "                await futures.create();   // F0\n" +
                "                intStorage.add(3);\n" +
                "                return 9;\n" +
                "            }\n" +
                "        }\n" +
                "        intStorage.add(999);\n" +
                "    } finally {\n" +
                "        intStorage.add(4);\n" +
                "        await futures.create();           // F1\n" +
                "        intStorage.add(5);\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n" +
                "intStorage.add(await func());\n" +
                "intStorage.add(777);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5, 9, 777), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    public static class ApiRoot {
        public static FutureHelper futures;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}