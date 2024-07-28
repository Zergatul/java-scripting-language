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

public class CompileConsumerTests {

    @BeforeEach
    public void clean() {
        ApiRoot.run = new Run();
        ApiRoot.futures = new FutureHelper();
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void intConsumerBasicTest() {
        String code = """
                intStorage.add(value);
                """;

        IntConsumer program = compile(ApiRoot.class, code, IntConsumer.class);
        program.accept(123);
        program.accept(321);

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(123, 321));
    }

    @Test
    public void intConsumerCapture1Test() {
        String code = """
                run.once(() => intStorage.add(value + 100));
                """;

        IntConsumer program = compile(ApiRoot.class, code, IntConsumer.class);
        program.accept(12);
        program.accept(13);
        program.accept(14);

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(112, 113, 114));
    }

    @Test
    public void intConsumerCapture2Test() {
        String code = """
                run.multiple(2, () => {
                    run.multiple(3, () => {
                        intStorage.add(value * value);
                    });
                });
                """;

        IntConsumer program = compile(ApiRoot.class, code, IntConsumer.class);
        program.accept(3);

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(9, 9, 9, 9, 9, 9));
    }

    @Test
    public void await1Test() {
        String code = """
                intStorage.add(value);
                await futures.create();
                """;

        IntConsumer program = compile(ApiRoot.class, code, IntConsumer.class);
        program.accept(2);

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2));
    }

    @Test
    public void await2Test() {
        String code = """
                intStorage.add(value);
                await futures.create();
                intStorage.add(value * value);
                await futures.create();
                intStorage.add(value * value * value);
                await futures.create();
                intStorage.add(value * value * value * value);
                """;

        IntConsumer program = compile(ApiRoot.class, code, IntConsumer.class);
        program.accept(2);

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2));
        ApiRoot.futures.get(0).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2, 4));
        ApiRoot.futures.get(1).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2, 4, 8));
        ApiRoot.futures.get(2).complete(null);
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2, 4, 8, 16));
    }

    @Test
    public void blockPosConsumerTest() {
        String code = """
                run.once(() => {
                    run.once(() => {
                        intStorage.add(x);
                        intStorage.add(y);
                        intStorage.add(z);
                    });
                });
                """;

        BlockPosConsumer program = compile(ApiRoot.class, code, BlockPosConsumer.class);
        program.accept(23, 24, 25);

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(23, 24, 25));
    }

    private static <T> T compile(Class<?> api, String code, Class<T> clazz) {
        Compiler compiler = new Compiler(new CompilationParameters(api, false));
        CompilationResult<T> result = compiler.compile(code, clazz);
        Assertions.assertNull(result.diagnostics());
        return result.program();
    }

    public static class ApiRoot {
        public static Run run;
        public static FutureHelper futures;
        public static IntStorage intStorage;
    }

    @FunctionalInterface
    public interface IntConsumer {
        void accept(int value);
    }

    public interface BlockPosConsumer {
        void accept(int x, int y, int z);
    }
}