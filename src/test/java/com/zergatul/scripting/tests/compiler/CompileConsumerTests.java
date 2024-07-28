package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.tests.compiler.helpers.FutureHelper;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.Run;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

public class CompileConsumerTests {

    @BeforeEach
    public void clean() {
        ApiRoot.run = new Run();
        ApiRoot.futures = new FutureHelper();
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void consumerIntSimpleTest() {
        String code = """
                intStorage.add(input);
                """;

        Consumer<Integer> program = compile(ApiRoot.class, code, "input", Integer.class);
        program.accept(123);
        program.accept(321);

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(123, 321));
    }

    @Test
    public void consumerIntCapture1Test() {
        String code = """
                run.once(() => intStorage.add(input + 100));
                """;

        Consumer<Integer> program = compile(ApiRoot.class, code, "input", Integer.class);
        program.accept(12);
        program.accept(13);
        program.accept(14);

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(112, 113, 114));
    }

    @Test
    public void consumerIntCapture2Test() {
        String code = """
                run.multiple(2, () => {
                    run.multiple(3, () => {
                        intStorage.add(x * x);
                    });
                });
                """;

        Consumer<Integer> program = compile(ApiRoot.class, code, "x", Integer.class);
        program.accept(3);

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(9, 9, 9, 9, 9, 9));
    }

    @Test
    public void awaitTest() {
        String code = """
                intStorage.add(x);
                await futures.create();
                intStorage.add(x * x);
                await futures.create();
                intStorage.add(x * x * x);
                await futures.create();
                intStorage.add(x * x * x * x);
                """;

        Consumer<Integer> program = compile(ApiRoot.class, code, "x", Integer.class);
        program.accept(2);

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2));
        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2, 4));
        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2, 4, 8));
        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2, 4, 8, 16));
    }

    private static <T> Consumer<T> compile(Class<?> api, String code, String parameterName, Class<T> clazz) {
        Compiler compiler = new Compiler(new CompilationParameters(api, true));
        CompilationResult<Consumer<T>> result = compiler.<T>compileConsumer(code, parameterName, clazz);
        Assertions.assertNull(result.diagnostics());
        return result.program();
    }

    public static class ApiRoot {
        public static Run run;
        public static FutureHelper futures;
        public static IntStorage intStorage;
    }
}