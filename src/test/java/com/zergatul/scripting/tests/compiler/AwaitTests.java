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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compileAsync;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class AwaitTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.futures = new FutureHelper();
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.longStorage = new Int64Storage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.run = new Run();
    }

    @Test
    public void awaitOutOfAsyncContextTest() {
        String code =
                "await futures.create();\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.AwaitInNonAsyncContext, new SingleLineTextRange(1, 1, 0, 5))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void noAwaitTest() {
        String code =
                "intStorage.add(123);\n" +
                "intStorage.add(456);\n" +
                "intStorage.add(789);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(123, 456, 789));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void simpleTest() {
        String code =
                "intStorage.add(123);\n" +
                "await futures.create();\n" +
                "intStorage.add(321);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(123));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(123, 321));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void captureVariableTest() {
        String code =
                "int x = 1;\n" +
                "intStorage.add(x);\n" +
                "await futures.create();\n" +
                "x++;\n" +
                "intStorage.add(x);\n" +
                "await futures.create();\n" +
                "x++;\n" +
                "intStorage.add(x);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 3));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void lambdaCapture1Test() {
        String code =
                "int x = 1;\n" +
                "await futures.create();\n" +
                "intStorage.add(x);\n" +
                "run.once(() => x++);\n" +
                "intStorage.add(x);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void lambdaCapture2Test() {
        String code =
                "int x = 1;\n" +
                "intStorage.add(x);\n" +
                "run.once(() => x++);\n" +
                "intStorage.add(x);\n" +
                "await futures.create();\n" +
                "x++;\n" +
                "intStorage.add(x);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 3));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void lambdaCapture3Test() {
        String code =
                "int x = 1;\n" +
                "intStorage.add(x);\n" +
                "int y = 4;\n" +
                "run.once(() => x += y);\n" +
                "intStorage.add(x);\n" +
                "await futures.create();\n" +
                "x++;\n" +
                "intStorage.add(x);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 5));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 5, 6));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void lambdaCapture4Test() {
        String code =
                "int x = 1;\n" +
                "intStorage.add(x);\n" +
                "\n" +
                "await futures.create();\n" +
                "\n" +
                "int y = 2;\n" +
                "run.multiple(2, () => {\n" +
                "    run.multiple(3, () => {\n" +
                "        run.multiple(4, () => {\n" +
                "            x += y;\n" +
                "        });\n" +
                "    });\n" +
                "});\n" +
                "intStorage.add(x);\n" +
                "\n" +
                "await futures.create();\n" +
                "\n" +
                "x++;\n" +
                "intStorage.add(x);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 49));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 49, 50));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if01Test() {
        String code =
                "if (true) {\n" +
                "    int x = 10;\n" +
                "    intStorage.add(x);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 1);\n" +
                "} else {\n" +
                "    int x = 20;\n" +
                "    intStorage.add(x);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 1);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 11));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if02Test() {
        String code =
                "if (false) {\n" +
                "    int x = 10;\n" +
                "    intStorage.add(x);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 1);\n" +
                "} else {\n" +
                "    int x = 20;\n" +
                "    intStorage.add(x);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 1);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(20));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(20, 21));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if03Test() {
        String code =
                "if (true) {\n" +
                "    int x = 10;\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 1);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 2);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 3);\n" +
                "} else {\n" +
                "    int x = 20;\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 1);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 2);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 3);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(11));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(11, 12));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(11, 12, 13));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if04Test() {
        String code =
                "if (false) {\n" +
                "    int x = 10;\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 1);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 2);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 3);\n" +
                "} else {\n" +
                "    int x = 20;\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 1);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 2);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x + 3);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(21));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(21, 22));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(21, 22, 23));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if05Test() {
        String code =
                "if (true) {\n" +
                "    int x = 10;\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if06Test() {
        String code =
                "if (false) {\n" +
                "    int x = 10;\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertEquals(ApiRoot.futures.getVoidCount(), 0);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if07Test() {
        String code =
                "if (true) {\n" +
                "    int x = 10;\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x);\n" +
                "} else {\n" +
                "    intStorage.add(100);\n" +
                "}\n" +
                "await futures.create();\n" +
                "intStorage.add(200);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 200));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if08Test() {
        String code =
                "if (false) {\n" +
                "    int x = 10;\n" +
                "    await futures.create();\n" +
                "    intStorage.add(x);\n" +
                "} else {\n" +
                "    intStorage.add(100);\n" +
                "}\n" +
                "await futures.create();\n" +
                "intStorage.add(200);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(100));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(100, 200));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if09Test() {
        String code =
                "if (await futures.createBool()) {\n" +
                "    intStorage.add(1);\n" +
                "} else {\n" +
                "    intStorage.add(2);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(true);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if10Test() {
        String code =
                "if (await futures.createBool()) {\n" +
                "    intStorage.add(1);\n" +
                "} else {\n" +
                "    intStorage.add(2);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(2));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if11Test() {
        String code =
                "if (await futures.createBool()) {\n" +
                "    intStorage.add(await futures.createInt());\n" +
                "} else {\n" +
                "    intStorage.add(2);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(true);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if12Test() {
        String code =
                "if (await futures.createBool()) {\n" +
                "    intStorage.add(await futures.createInt());\n" +
                "} else {\n" +
                "    intStorage.add(2);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(2));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void intFuture1Test() {
        String code =
                "int x = await futures.createInt();\n" +
                "intStorage.add(x);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(100);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(100));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void intFuture2Test() {
        String code =
                "int a1 = await futures.createInt();\n" +
                "int a2 = await futures.createInt();\n" +
                "intStorage.add(a1 + a2);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(30));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void unaryExpression1Test() {
        String code =
                "int x = -await futures.createInt();\n" +
                "intStorage.add(x);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(-10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void unaryExpression2Test() {
        String code =
                "boolean b = !await futures.createBool() && !await futures.createBool();\n" +
                "intStorage.add(b ? 1 : 2);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(1).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void binaryExpression1Test() {
        String code =
                "int x = await futures.createInt() + await futures.createInt();\n" +
                "intStorage.add(x);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(30));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void binaryExpression2Test() {
        String code =
                "int x = await futures.createInt() + 20;\n" +
                "intStorage.add(x);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(30));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void binaryExpression3Test() {
        String code =
                "int x = 10 + await futures.createInt();\n" +
                "intStorage.add(x);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(30));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void methodInvocationTest() {
        String code =
                "intStorage.add(await futures.createInt() + await futures.createInt());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(30));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void forLoop1Test() {
        String code =
                "for (int i = 0; i < 100; i++) {\n" +
                "    intStorage.add(i + await futures.createInt());\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        List<Integer> list = new ArrayList<>();
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, list);
        Assertions.assertFalse(future.isDone());

        for (int i = 0; i < 100; i++) {
            ApiRoot.futures.getInt(i).complete(i * 100);
            list.add(i * 100 + i);
            Assertions.assertIterableEquals(ApiRoot.intStorage.list, list);

            if (i < 99) {
                Assertions.assertFalse(future.isDone());
            } else {
                Assertions.assertTrue(future.isDone());
            }
        }
    }

    @Test
    public void forLoop2Test() {
        String code =
                "for (int i = await futures.createInt(); i < 10; i++) {\n" +
                "    intStorage.add(i + await futures.createInt());\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        List<Integer> list = new ArrayList<>();
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, list);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(5);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, list);
        for (int i = 0; i < 5; i++) {
            ApiRoot.futures.getInt(1 + i).complete(10);
            list.add(15 + i);
            Assertions.assertIterableEquals(ApiRoot.intStorage.list, list);

            if (i < 4) {
                Assertions.assertFalse(future.isDone());
            } else {
                Assertions.assertTrue(future.isDone());
            }
        }
    }

    @Test
    public void forLoop3Test() {
        String code =
                "for (int i = 0; await futures.createBool(); i++) {\n" +
                "    intStorage.add(await futures.createInt());\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(true);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(1).complete(true);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 20));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(2).complete(true);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 20));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(2).complete(30);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 20, 30));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(3).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 20, 30));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void forLoop4Test() {
        String code =
                "for (int i = 0; i < 100; i += await futures.createInt()) {\n" +
                "    intStorage.add(i);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(0));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(0, 10));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(0, 10, 30));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(2).complete(30);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(0, 10, 30, 60));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(3).complete(40);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(0, 10, 30, 60));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void forLoopBreakTest() {
        String code =
                "int sum = 0;\n" +
                "for (int i = 0; ; i += 1) {\n" +
                "    if (i == 3) break;\n" +
                "    sum += await futures.createInt();\n" +
                "}\n" +
                "intStorage.add(sum);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(0).complete(5);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(1).complete(10);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(2).complete(15);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(30));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void forLoopContinueTest() {
        String code =
                "int sum = 0;\n" +
                "for (int i = 0; i < 5; i++) {\n" +
                "    if (i % 2 == 0) continue;\n" +
                "    sum += await futures.createInt();\n" +
                "}\n" +
                "intStorage.add(sum);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(1).complete(11);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(21));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void forLoopNestedTest() {
        String code =
                "int sum = 0;\n" +
                "for (int i = 0; i < 3; i++) {\n" +
                "    for (int j = 0; i < 10; j++) {\n" +
                "        if (j == 5) break;\n" +
                "    }\n" +
                "    sum += await futures.createInt();\n" +
                "}\n" +
                "intStorage.add(sum);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(0).complete(11);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(1).complete(12);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(2).complete(13);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(36));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void forEachLoopTest() {
        String code =
                "int[] array = new int[100];\n" +
                "for (int i = 0; i < array.length; i++) {\n" +
                "    array[i] = i + 1;\n" +
                "}\n" +
                "foreach (int value in array) {\n" +
                "    intStorage.add(value + await futures.createInt());\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        List<Integer> list = new ArrayList<>();
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, list);
        Assertions.assertFalse(future.isDone());
        for (int i = 0; i < 100; i++) {
            ApiRoot.futures.getInt(i).complete((i + 1) * 100);
            list.add((i + 1) * 100 + (i + 1));
            Assertions.assertIterableEquals(ApiRoot.intStorage.list, list);

            if (i < 99) {
                Assertions.assertFalse(future.isDone());
            } else {
                Assertions.assertTrue(future.isDone());
            }
        }
    }

    @Test
    public void forEachLoopBreakTest() {
        String code =
                "foreach (int value in new int[] { 1, 2, 3, 4, 5 }) {\n" +
                "    intStorage.add(value + await futures.createInt());\n" +
                "    if (value == 3) break;\n" +
                "}\n" +
                "intStorage.add(10);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(11));
        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(11, 22));
        ApiRoot.futures.getInt(2).complete(30);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(11, 22, 33, 10));
    }

    @Test
    public void forEachLoopContinueTest() {
        String code =
                "foreach (int value in new int[] { 1, 2, 3, 4, 5 }) {\n" +
                "    if (value % 2 == 0) continue;\n" +
                "    intStorage.add(value + await futures.createInt());\n" +
                "}\n" +
                "intStorage.add(10);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(11));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(11, 23));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(2).complete(30);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(11, 23, 35, 10));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void whileLoopTest() {
        String code =
                "while (await futures.createBool()) {\n" +
                "    intStorage.add(await futures.createInt());\n" +
                "}\n" +
                "intStorage.add(100);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(true);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(1).complete(true);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 20));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(2).complete(true);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(2).complete(30);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 20, 30));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(3).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 20, 30, 100));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void whileLoopBreakTest() {
        String code =
                "while (true) {\n" +
                "    int x = await futures.createInt();\n" +
                "    if (x >= 3) break;\n" +
                "    intStorage.add(x);\n" +
                "}\n" +
                "intStorage.add(100);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(1);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(2);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(2).complete(3);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2, 100));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void whileLoopContinueTest() {
        String code =
                "while (true) {\n" +
                "    int x = await futures.createInt();\n" +
                "    if (x % 2 == 0) continue;\n" +
                "    intStorage.add(x);\n" +
                "    if (x >= 5) break;\n" +
                "}\n" +
                "intStorage.add(100);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(1);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(2);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(2).complete(3);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 3));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(3).complete(4);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 3));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(4).complete(5);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 3, 5, 100));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void return1Test() {
        String code =
                "if (await futures.createBool()) {\n" +
                "    return;\n" +
                "}\n" +
                "intStorage.add(100);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(true);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of());
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void return2Test() {
        String code =
                "if (await futures.createBool()) {\n" +
                "    return;\n" +
                "}\n" +
                "intStorage.add(100);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(100));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void longTest() {
        String code =
                "for (int i = 0; i < 3; i++) {\n" +
                "    longStorage.add(await futures.createLong());\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getLong(0).complete(111L);
        Assertions.assertIterableEquals(ApiRoot.longStorage.list, Lists.of(111L));

        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getLong(1).complete(222L);
        Assertions.assertIterableEquals(ApiRoot.longStorage.list, Lists.of(111L, 222L));

        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getLong(2).complete(333L);
        Assertions.assertIterableEquals(ApiRoot.longStorage.list, Lists.of(111L, 222L, 333L));

        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void methodInvocationAwaitObjectTest() {
        String code =
                "stringStorage.add((await futures.createBool()).toString());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(false);
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("false"));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void futureMethodsTest() {
        String code =
                "let future = futures.create();\n" +
                "boolStorage.add(future.isDone());\n" +
                "await future;\n" +
                "boolStorage.add(future.isDone());\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(false));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(false, true));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void cancellationStopsContinuationTest() {
        String code =
                "intStorage.add(1);\n" +
                "await futures.create();\n" +
                "intStorage.add(2);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertTrue(future.cancel(false));
        Assertions.assertTrue(future.isCancelled());
        Assertions.assertTrue(ApiRoot.futures.get(0).isCancelled());
        Assertions.assertFalse(ApiRoot.futures.get(0).complete(null));
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
    }

    @Test
    public void cancellationStopsLaterContinuationTest() {
        String code =
                "intStorage.add(1);\n" +
                "await futures.create();\n" +
                "intStorage.add(2);\n" +
                "await futures.create();\n" +
                "intStorage.add(3);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2));
        Assertions.assertEquals(2, ApiRoot.futures.getVoidCount());

        Assertions.assertTrue(future.cancel(false));
        Assertions.assertTrue(ApiRoot.futures.get(1).isCancelled());
        Assertions.assertFalse(ApiRoot.futures.get(1).complete(null));
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2));
    }

    @Test
    public void cancellationSkipsFinallyTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    await futures.create();\n" +
                "    intStorage.add(2);\n" +
                "} finally {\n" +
                "    intStorage.add(3);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
        Assertions.assertTrue(future.cancel(false));
        Assertions.assertTrue(ApiRoot.futures.get(0).isCancelled());
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1));
    }

    @Test
    public void javaTypeFutureMethod() {
        String code =
                "typealias TestClass = Java<com.zergatul.scripting.tests.compiler.AwaitTests$TestClass>;\n" +
                "\n" +
                "let future1 = TestClass.externalMethod();\n" +
                "let result = await future1;\n" +
                "stringStorage.add(result);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("Hello"));
        Assertions.assertTrue(future.isDone());
    }

    public static class ApiRoot {
        public static FutureHelper futures;
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static Int64Storage longStorage;
        public static StringStorage stringStorage;
        public static Run run;
    }

    public static class TestClass {
        public static CompletableFuture<String> externalMethod() {
            return CompletableFuture.completedFuture("Hello");
        }
    }
}