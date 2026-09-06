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

public class TryStatementAsyncTryCatchFinallyTests {

    @BeforeEach
    public void clean() {
        ApiRoot.futures = new FutureHelper();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
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
                "} finally {\n" +
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
                "} finally {\n" +
                "    intStorage.add(5);\n" +
                "}\n" +
                "intStorage.add(6);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 4, 5, 6));
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
                "        throw;\n" +
                "    } finally {\n" +
                "        intStorage.add(5);\n" +
                "    }\n" +
                "    intStorage.add(6);\n" +
                "} catch {\n" +
                "    intStorage.add(7);\n" +
                "} finally {\n" +
                "    intStorage.add(8);\n" +
                "}\n" +
                "intStorage.add(9);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5, 7, 8, 9), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void sequentialTryCatchFinallyBlocksTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create();              // F0\n" +
                "    intStorage.add(2);\n" +
                "} catch {\n" +
                "    intStorage.add(999);\n" +
                "} finally {\n" +
                "    intStorage.add(3);\n" +
                "    await futures.create();              // F1\n" +
                "    intStorage.add(4);\n" +
                "}\n" +
                "\n" +
                "try {\n" +
                "    intStorage.add(5);\n" +
                "    await futures.create();              // F2\n" +
                "    [1][2] = 3;                          // throws\n" +
                "    intStorage.add(999);\n" +
                "} catch {\n" +
                "    intStorage.add(6);\n" +
                "    await futures.create();              // F3\n" +
                "    intStorage.add(7);\n" +
                "} finally {\n" +
                "    intStorage.add(8);\n" +
                "    await futures.create();              // F4\n" +
                "    intStorage.add(9);\n" +
                "}\n" +
                "\n" +
                "intStorage.add(10);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5, 6), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(3).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5, 6, 7, 8), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(4).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void returnFromCatchThroughNestedFinallyTest() {
        String code =
                "async int func() {\n" +
                "    try {\n" +
                "        try {\n" +
                "            intStorage.add(1);\n" +
                "            await futures.create();      // F0\n" +
                "            [1][2] = 3;                  // throws\n" +
                "            intStorage.add(999);\n" +
                "        } catch {\n" +
                "            intStorage.add(2);\n" +
                "            await futures.create();      // F1\n" +
                "            return 50;\n" +
                "        } finally {\n" +
                "            intStorage.add(3);\n" +
                "            await futures.create();      // F2\n" +
                "            intStorage.add(4);\n" +
                "        }\n" +
                "    } finally {\n" +
                "        intStorage.add(5);\n" +
                "        await futures.create();          // F3\n" +
                "        intStorage.add(6);\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(await func());\n" +
                "intStorage.add(7);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(3).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5, 6, 50, 7), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void exceptionFromCatchThroughNestedFinallyTest() {
        String code =
                "try {\n" +
                "    try {\n" +
                "        intStorage.add(1);\n" +
                "        await futures.create();          // F0\n" +
                "        [1][2] = 3;                      // throws\n" +
                "        intStorage.add(999);\n" +
                "    } catch {\n" +
                "        intStorage.add(2);\n" +
                "        await futures.create();          // F1\n" +
                "        [1][2] = 4;                      // throws\n" +
                "        intStorage.add(999);\n" +
                "    } finally {\n" +
                "        intStorage.add(3);\n" +
                "        await futures.create();          // F2\n" +
                "        intStorage.add(4);\n" +
                "    }\n" +
                "} catch {\n" +
                "    intStorage.add(5);\n" +
                "} finally {\n" +
                "    intStorage.add(6);\n" +
                "    await futures.create();              // F3\n" +
                "    intStorage.add(7);\n" +
                "}\n" +
                "\n" +
                "intStorage.add(8);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5, 6), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(3).complete(null);
        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5, 6, 7, 8), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void jumpFromCatchOuterFinallyTest() {
        String code =
                "for (int i = 0; i < 3; i++) {\n" +
                "    try {\n" +
                "        try {\n" +
                "            intStorage.add(i);\n" +
                "            await futures.create();          // F0, F2, F4...\n" +
                "            if (i == 0) {\n" +
                "                intStorage.add(10);\n" +
                "            }\n" +
                "        } finally {\n" +
                "            intStorage.add(100 + i);\n" +
                "            await futures.create();          // F1, F3, F5...\n" +
                "            if (i == 0) {\n" +
                "                [1][2] = 3;                  // throws\n" +
                "            }\n" +
                "            intStorage.add(200 + i);\n" +
                "        }\n" +
                "    } catch {\n" +
                "        intStorage.add(300 + i);\n" +
                "        continue;\n" +
                "    } finally {\n" +
                "        intStorage.add(400 + i);\n" +
                "        await futures.create();              // F? (only for iterations that reach here)\n" +
                "        intStorage.add(500 + i);\n" +
                "    }\n" +
                "    intStorage.add(600 + i);\n" +
                "}\n" +
                "intStorage.add(9999);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(Lists.of(0), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100, 300, 400), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100, 300, 400, 500, 1), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(3).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100, 300, 400, 500, 1, 101), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(4).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100, 300, 400, 500, 1, 101, 201, 401), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(5).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100, 300, 400, 500, 1, 101, 201, 401, 501, 601, 2), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(6).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100, 300, 400, 500, 1, 101, 201, 401, 501, 601, 2, 102), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(7).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100, 300, 400, 500, 1, 101, 201, 401, 501, 601, 2, 102, 202, 402), ApiRoot.intStorage.list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(8).complete(null);
        Assertions.assertIterableEquals(Lists.of(0, 10, 100, 300, 400, 500, 1, 101, 201, 401, 501, 601, 2, 102, 202, 402, 502, 602, 9999), ApiRoot.intStorage.list);
        Assertions.assertTrue(future.isDone());
    }

    public static class ApiRoot {
        public static FutureHelper futures;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}