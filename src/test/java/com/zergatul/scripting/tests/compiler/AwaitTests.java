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
        var x = ApiRoot.futures.manuals.get(0).whenCompleteAsync((r, e) -> {});
        ApiRoot.futures.manuals.get(0).complete(Void.TYPE.cast(null));
        x.join();
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123, 321));
    }

    public static class ApiRoot {
        public static FutureHelper futures;
        public static IntStorage intStorage;
    }
}