package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.FutureHelper;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
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

    public static class ApiRoot {
        public static FutureHelper futures;
        public static IntStorage intStorage;
    }
}