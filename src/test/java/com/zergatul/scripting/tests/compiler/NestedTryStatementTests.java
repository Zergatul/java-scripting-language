package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class NestedTryStatementTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void nestedTryFinallyExceptionCaughtByOuterCatchOrderingTest() {
        String code = """
                intStorage.add(1);
                try {
                    intStorage.add(2);
                    try {
                        intStorage.add(3);
                        (new int[0])[1] = 10; // throws
                        intStorage.add(999);
                    } finally {
                        intStorage.add(4);
                    }
                    intStorage.add(9999);
                } catch {
                    intStorage.add(5);
                } finally {
                    intStorage.add(6);
                }
                intStorage.add(7);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(1, 2, 3, 4, 5, 6, 7), ApiRoot.intStorage.list);
    }

    @Test
    public void nestedRethrowCaughtByOuterCatchTest() {
        String code = """
                intStorage.add(1);
                try {
                    try {
                        intStorage.add(2);
                        [1][2] = 3; // throws
                    } catch {
                        intStorage.add(3);
                        throw;
                    } finally {
                        intStorage.add(4);
                    }
                    intStorage.add(999);
                } catch {
                    intStorage.add(5);
                } finally {
                    intStorage.add(6);
                }
                intStorage.add(7);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(1, 2, 3, 4, 5, 6, 7), ApiRoot.intStorage.list);
    }

    @Test
    public void nestedTryFinallyWithOuterLoopBreakTest() {
        String code = """
                intStorage.add(1);
                foreach (int x in [2, 3, 4]) {
                    try {
                        try {
                            intStorage.add(x);
                            if (x == 3) {
                                break;
                            }
                        } finally {
                            intStorage.add(100 + x);
                        }
                    } finally {
                        intStorage.add(200 + x);
                    }
                }
                intStorage.add(9);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(1, 2, 102, 202, 3, 103, 203, 9), ApiRoot.intStorage.list);
    }

    @Test
    public void nestedFinallyAndReturnTest() {
        String code = """
                try {
                    intStorage.add(1);
                    try {
                        intStorage.add(2);
                        return;
                    } finally {
                        intStorage.add(3);
                    }
                } finally {
                    intStorage.add(4);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(1, 2, 3, 4), ApiRoot.intStorage.list);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}