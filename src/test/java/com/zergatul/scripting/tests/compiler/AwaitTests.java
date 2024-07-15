package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.FutureHelper;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.Run;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class AwaitTests {

    @BeforeEach
    public void clean() {
        ApiRoot.futures = new FutureHelper();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.run = new Run();
    }

    @Test
    public void simpleTest() {
        String code = """
                intStorage.add(123);
                await futures.manual();
                intStorage.add(321);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123));
        ApiRoot.futures.getManualFuture(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123, 321));
    }

    @Test
    public void captureVariableTest() {
        String code = """
                int x = 1;
                intStorage.add(x);
                await futures.manual();
                x++;
                intStorage.add(x);
                await futures.manual();
                x++;
                intStorage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1));
        ApiRoot.futures.getManualFuture(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2));
        ApiRoot.futures.getManualFuture(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3));
    }

    @Test
    public void lambdaCapture1Test() {
        String code = """
                int x = 1;
                await futures.manual();
                intStorage.add(x);
                run.once(() => x++);
                intStorage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        ApiRoot.futures.getManualFuture(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2));
    }

    @Test
    public void lambdaCapture2Test() {
        String code = """
                int x = 1;
                intStorage.add(x);
                run.once(() => x++);
                intStorage.add(x);
                await futures.manual();
                x++;
                intStorage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2));
        ApiRoot.futures.getManualFuture(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 3));
    }

    // TODO: lambda capture depth 2+?

    @Test
    public void If1Test() {
        String code = """
                if (true) {
                    int x = 10;
                    intStorage.add(x);
                    await futures.manual();
                    intStorage.add(x + 1);
                } else {
                    int x = 20;
                    intStorage.add(x);
                    await futures.manual();
                    intStorage.add(x + 1);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10));
        ApiRoot.futures.getManualFuture(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 11));
    }

    @Test
    public void If2Test() {
        String code = """
                if (false) {
                    int x = 10;
                    intStorage.add(x);
                    await futures.manual();
                    intStorage.add(x + 1);
                } else {
                    int x = 20;
                    intStorage.add(x);
                    await futures.manual();
                    intStorage.add(x + 1);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(20));
        ApiRoot.futures.getManualFuture(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(20, 21));
    }

    @Test
    public void If3Test() {
        String code = """
                if (true) {
                    int x = 10;
                    await futures.manual();
                    intStorage.add(x + 1);
                    await futures.manual();
                    intStorage.add(x + 2);
                    await futures.manual();
                    intStorage.add(x + 3);
                } else {
                    int x = 20;
                    await futures.manual();
                    intStorage.add(x + 1);
                    await futures.manual();
                    intStorage.add(x + 2);
                    await futures.manual();
                    intStorage.add(x + 3);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        ApiRoot.futures.getManualFuture(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11));
        ApiRoot.futures.getManualFuture(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11, 12));
        ApiRoot.futures.getManualFuture(2).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11, 12, 13));
    }

    @Test
    public void If4Test() {
        String code = """
                if (false) {
                    int x = 10;
                    await futures.manual();
                    intStorage.add(x + 1);
                    await futures.manual();
                    intStorage.add(x + 2);
                    await futures.manual();
                    intStorage.add(x + 3);
                } else {
                    int x = 20;
                    await futures.manual();
                    intStorage.add(x + 1);
                    await futures.manual();
                    intStorage.add(x + 2);
                    await futures.manual();
                    intStorage.add(x + 3);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        ApiRoot.futures.getManualFuture(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(21));
        ApiRoot.futures.getManualFuture(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(21, 22));
        ApiRoot.futures.getManualFuture(2).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(21, 22, 23));
    }

    @Test
    public void If5Test() {
        String code = """
                if (true) {
                    int x = 10;
                    await futures.manual();
                    intStorage.add(x);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        ApiRoot.futures.getManualFuture(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10));
    }

    @Test
    public void If6Test() {
        String code = """
                if (false) {
                    int x = 10;
                    await futures.manual();
                    intStorage.add(x);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        Assertions.assertEquals(ApiRoot.futures.getManualFuturesCount(), 0);
    }

    @Test
    public void If7Test() {
        String code = """
                if (true) {
                    int x = 10;
                    await futures.manual();
                    intStorage.add(x);
                } else {
                    intStorage.add(100);
                }
                await futures.manual();
                intStorage.add(200);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of());
        ApiRoot.futures.getManualFuture(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10));
        ApiRoot.futures.getManualFuture(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 200));
    }

    @Test
    public void If8Test() {
        String code = """
                if (false) {
                    int x = 10;
                    await futures.manual();
                    intStorage.add(x);
                } else {
                    intStorage.add(100);
                }
                await futures.manual();
                intStorage.add(200);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(100));
        ApiRoot.futures.getManualFuture(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(100, 200));
    }

    public static class ApiRoot {
        public static FutureHelper futures;
        public static IntStorage intStorage;
        public static Run run;
    }
}