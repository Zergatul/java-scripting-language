package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.AsyncRunnable;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.FutureHelper;
import com.zergatul.scripting.tests.compiler.helpers.Int64Storage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.Run;
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
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.longStorage = new Int64Storage();
        ApiRoot.run = new Run();
    }

    @Test
    public void awaitOutOfAsyncContextTest() {
        String code = """
                await futures.create();
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(BinderErrors.AwaitInNonAsyncContext, new SingleLineTextRange(1, 1, 0, 5))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void noAwaitTest() {
        String code = """
                intStorage.add(123);
                intStorage.add(456);
                intStorage.add(789);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123, 456, 789));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void simpleTest() {
        String code = """
                intStorage.add(123);
                await futures.create();
                intStorage.add(321);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123, 321));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void captureVariableTest() {
        String code = """
                int x = 1;
                intStorage.add(x);
                await futures.create();
                x++;
                intStorage.add(x);
                await futures.create();
                x++;
                intStorage.add(x);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void lambdaCapture1Test() {
        String code = """
                int x = 1;
                await futures.create();
                intStorage.add(x);
                run.once(() => x++);
                intStorage.add(x);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void lambdaCapture2Test() {
        String code = """
                int x = 1;
                intStorage.add(x);
                run.once(() => x++);
                intStorage.add(x);
                await futures.create();
                x++;
                intStorage.add(x);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void lambdaCapture3Test() {
        String code = """
                int x = 1;
                intStorage.add(x);
                int y = 4;
                run.once(() => x += y);
                intStorage.add(x);
                await futures.create();
                x++;
                intStorage.add(x);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 5));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 5, 6));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void lambdaCapture4Test() {
        String code = """
                int x = 1;
                intStorage.add(x);
                
                await futures.create();
                
                int y = 2;
                run.multiple(2, () => {
                    run.multiple(3, () => {
                        run.multiple(4, () => {
                            x += y;
                        });
                    });
                });
                intStorage.add(x);
                
                await futures.create();
                
                x++;
                intStorage.add(x);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 49));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 49, 50));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if01Test() {
        String code = """
                if (true) {
                    int x = 10;
                    intStorage.add(x);
                    await futures.create();
                    intStorage.add(x + 1);
                } else {
                    int x = 20;
                    intStorage.add(x);
                    await futures.create();
                    intStorage.add(x + 1);
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 11));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if02Test() {
        String code = """
                if (false) {
                    int x = 10;
                    intStorage.add(x);
                    await futures.create();
                    intStorage.add(x + 1);
                } else {
                    int x = 20;
                    intStorage.add(x);
                    await futures.create();
                    intStorage.add(x + 1);
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(20));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(20, 21));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if03Test() {
        String code = """
                if (true) {
                    int x = 10;
                    await futures.create();
                    intStorage.add(x + 1);
                    await futures.create();
                    intStorage.add(x + 2);
                    await futures.create();
                    intStorage.add(x + 3);
                } else {
                    int x = 20;
                    await futures.create();
                    intStorage.add(x + 1);
                    await futures.create();
                    intStorage.add(x + 2);
                    await futures.create();
                    intStorage.add(x + 3);
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11, 12));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11, 12, 13));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if04Test() {
        String code = """
                if (false) {
                    int x = 10;
                    await futures.create();
                    intStorage.add(x + 1);
                    await futures.create();
                    intStorage.add(x + 2);
                    await futures.create();
                    intStorage.add(x + 3);
                } else {
                    int x = 20;
                    await futures.create();
                    intStorage.add(x + 1);
                    await futures.create();
                    intStorage.add(x + 2);
                    await futures.create();
                    intStorage.add(x + 3);
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(21));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(21, 22));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(21, 22, 23));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if05Test() {
        String code = """
                if (true) {
                    int x = 10;
                    await futures.create();
                    intStorage.add(x);
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if06Test() {
        String code = """
                if (false) {
                    int x = 10;
                    await futures.create();
                    intStorage.add(x);
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertEquals(ApiRoot.futures.getVoidCount(), 0);
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if07Test() {
        String code = """
                if (true) {
                    int x = 10;
                    await futures.create();
                    intStorage.add(x);
                } else {
                    intStorage.add(100);
                }
                await futures.create();
                intStorage.add(200);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 200));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if08Test() {
        String code = """
                if (false) {
                    int x = 10;
                    await futures.create();
                    intStorage.add(x);
                } else {
                    intStorage.add(100);
                }
                await futures.create();
                intStorage.add(200);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(100));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(100, 200));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if09Test() {
        String code = """
                if (await futures.createBool()) {
                    intStorage.add(1);
                } else {
                    intStorage.add(2);
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(true);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if10Test() {
        String code = """
                if (await futures.createBool()) {
                    intStorage.add(1);
                } else {
                    intStorage.add(2);
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if11Test() {
        String code = """
                if (await futures.createBool()) {
                    intStorage.add(await futures.createInt());
                } else {
                    intStorage.add(2);
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(true);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void if12Test() {
        String code = """
                if (await futures.createBool()) {
                    intStorage.add(await futures.createInt());
                } else {
                    intStorage.add(2);
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void intFuture1Test() {
        String code = """
                int x = await futures.createInt();
                intStorage.add(x);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(100);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(100));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void intFuture2Test() {
        String code = """
                int a1 = await futures.createInt();
                int a2 = await futures.createInt();
                intStorage.add(a1 + a2);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(30));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void unaryExpression1Test() {
        String code = """
                int x = -await futures.createInt();
                intStorage.add(x);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(-10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void unaryExpression2Test() {
        String code = """
                boolean b = !await futures.createBool() && !await futures.createBool();
                intStorage.add(b ? 1 : 2);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(1).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void binaryExpression1Test() {
        String code = """
                int x = await futures.createInt() + await futures.createInt();
                intStorage.add(x);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(30));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void binaryExpression2Test() {
        String code = """
                int x = await futures.createInt() + 20;
                intStorage.add(x);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(30));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void binaryExpression3Test() {
        String code = """
                int x = 10 + await futures.createInt();
                intStorage.add(x);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(30));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void methodInvocationTest() {
        String code = """
                intStorage.add(await futures.createInt() + await futures.createInt());
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(30));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void forLoop1Test() {
        String code = """
                for (int i = 0; i < 100; i++) {
                    intStorage.add(i + await futures.createInt());
                }
                """;

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
        String code = """
                for (int i = await futures.createInt(); i < 10; i++) {
                    intStorage.add(i + await futures.createInt());
                }
                """;

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
        String code = """
                for (int i = 0; await futures.createBool(); i++) {
                    intStorage.add(await futures.createInt());
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(true);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(1).complete(true);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 20));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(2).complete(true);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 20));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(2).complete(30);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 20, 30));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(3).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 20, 30));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void forLoop4Test() {
        String code = """
                for (int i = 0; i < 100; i += await futures.createInt()) {
                    intStorage.add(i);
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(0));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(0, 10));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(0, 10, 30));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(2).complete(30);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(0, 10, 30, 60));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(3).complete(40);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(0, 10, 30, 60));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void forLoopBreakTest() {
        String code = """
                int sum = 0;
                for (int i = 0; ; i += 1) {
                    if (i == 3) break;
                    sum += await futures.createInt();
                }
                intStorage.add(sum);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(0).complete(5);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(1).complete(10);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(2).complete(15);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(30));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void forLoopContinueTest() {
        String code = """
                int sum = 0;
                for (int i = 0; i < 5; i++) {
                    if (i % 2 == 0) continue;
                    sum += await futures.createInt();
                }
                intStorage.add(sum);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(1).complete(11);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(21));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void forLoopNestedTest() {
        String code = """
                int sum = 0;
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; i < 10; j++) {
                        if (j == 5) break;
                    }
                    sum += await futures.createInt();
                }
                intStorage.add(sum);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(0).complete(11);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(1).complete(12);
        Assertions.assertFalse(future.isDone());
        ApiRoot.futures.getInt(2).complete(13);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(36));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void forEachLoopTest() {
        String code = """
                int[] array = new int[100];
                for (int i = 0; i < array.length; i++) {
                    array[i] = i + 1;
                }
                foreach (int value in array) {
                    intStorage.add(value + await futures.createInt());
                }
                """;

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
        String code = """
                foreach (int value in new int[] { 1, 2, 3, 4, 5 }) {
                    intStorage.add(value + await futures.createInt());
                    if (value == 3) break;
                }
                intStorage.add(10);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11));
        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11, 22));
        ApiRoot.futures.getInt(2).complete(30);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11, 22, 33, 10));
    }

    @Test
    public void forEachLoopContinueTest() {
        String code = """
                foreach (int value in new int[] { 1, 2, 3, 4, 5 }) {
                    if (value % 2 == 0) continue;
                    intStorage.add(value + await futures.createInt());
                }
                intStorage.add(10);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11, 23));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(2).complete(30);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11, 23, 35, 10));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void whileLoopTest() {
        String code = """
                while (await futures.createBool()) {
                    intStorage.add(await futures.createInt());
                }
                intStorage.add(100);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(true);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(10);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(1).complete(true);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(20);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 20));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(2).complete(true);
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(2).complete(30);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 20, 30));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(3).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 20, 30, 100));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void whileLoopBreakTest() {
        String code = """
                while (true) {
                    int x = await futures.createInt();
                    if (x >= 3) break;
                    intStorage.add(x);
                }
                intStorage.add(100);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(1);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(2);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(2).complete(3);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 100));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void whileLoopContinueTest() {
        String code = """
                while (true) {
                    int x = await futures.createInt();
                    if (x % 2 == 0) continue;
                    intStorage.add(x);
                    if (x >= 5) break;
                }
                intStorage.add(100);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(0).complete(1);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(1).complete(2);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(2).complete(3);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 3));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(3).complete(4);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 3));
        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getInt(4).complete(5);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 3, 5, 100));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void return1Test() {
        String code = """
                if (await futures.createBool()) {
                    return;
                }
                intStorage.add(100);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(true);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void return2Test() {
        String code = """
                if (await futures.createBool()) {
                    return;
                }
                intStorage.add(100);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getBool(0).complete(false);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(100));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void longTest() {
        String code = """
                for (int i = 0; i < 3; i++) {
                    longStorage.add(await futures.createLong());
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getLong(0).complete(111L);
        Assertions.assertIterableEquals(ApiRoot.longStorage.list, List.of(111L));

        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getLong(1).complete(222L);
        Assertions.assertIterableEquals(ApiRoot.longStorage.list, List.of(111L, 222L));

        Assertions.assertFalse(future.isDone());

        ApiRoot.futures.getLong(2).complete(333L);
        Assertions.assertIterableEquals(ApiRoot.longStorage.list, List.of(111L, 222L, 333L));

        Assertions.assertTrue(future.isDone());
    }

    public static class ApiRoot {
        public static FutureHelper futures;
        public static IntStorage intStorage;
        public static Int64Storage longStorage;
        public static Run run;
    }
}