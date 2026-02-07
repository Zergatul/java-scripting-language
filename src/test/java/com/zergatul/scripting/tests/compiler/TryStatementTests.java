package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class TryBlockTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void emptyTryCatchTest() {
        String code = """
                try {}
                catch {}
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();
    }

    @Test
    public void emptyTryFinallyTest() {
        String code = """
                try {}
                finally {}
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();
    }

    @Test
    public void emptyTryCatchFinallyTest() {
        String code = """
                try {}
                catch {}
                finally {}
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();
    }

    @Test
    public void tryCatchNoExceptionTest() {
        String code = """
                intStorage.add(1);
                try {
                    intStorage.add(2);
                } catch {
                    intStorage.add(3);
                }
                intStorage.add(4);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 4));
    }

    @Test
    public void tryCatchWithExceptionTest() {
        String code = """
                intStorage.add(1);
                try {
                    int[] array = [];
                    intStorage.add(2);
                    array[1] = 10;
                    intStorage.add(3);
                } catch {
                    intStorage.add(4);
                }
                intStorage.add(5);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 4, 5));
    }

    @Test
    public void tryCatchWithExceptionVariableTest() {
        String code = """
                try {
                    int[] array = [];
                    array[1] = 10;
                } catch (e) {
                    stringStorage.add(e.getMessage());
                    stringStorage.add(e.toString());
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of(
                "Index 1 out of bounds for length 0",
                "java.lang.ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 0"));
    }

    @Test
    public void simpleTryCatchFinallyTest() {
        String code = """
                intStorage.add(1);
                try {
                    intStorage.add(2);
                } catch {
                    intStorage.add(3);
                } finally {
                    intStorage.add(4);
                }
                intStorage.add(5);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 2, 4, 5));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}