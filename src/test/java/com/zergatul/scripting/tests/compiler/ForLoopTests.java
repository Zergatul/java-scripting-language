package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class ForLoopTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
    }

    @Test
    public void breakStatementTest() {
        String code = """
                for (;;) {
                    intStorage.add(10);
                    break;
                    intStorage.add(20);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(10));
    }

    @Test
    public void continueStatementTest() {
        String code = """
                int sum;
                for (int i = 10; i >= 0; i--) {
                    if (i > 5) {
                        continue;
                    }
                    sum = sum + i;
                }
                intStorage.add(sum);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(15));
    }

    @Test
    public void loopArrayAssignTest() {
        String code = """
                int[] a = new int[10];
                for (int i = 0; i < a.length; i++) {
                    a[i] = i + 1;
                }
                for (int i = 0; i < a.length; i++) {
                    intStorage.add(a[i]);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void noInitializerTest() {
        String code = """
                int sum = 0;
                int j = 0;
                for (; j <= 10;) {
                    sum = sum + j;
                    j = j + 1;
                }
                intStorage.add(sum);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(55));
    }

    @Test
    public void backwardsTest() {
        String code = """
                int result = 1;
                for (int i = 10; i >= 0; i--) {
                    int mode = i % 3;
                    if (mode == 0) {
                        result = result + i;
                    }
                    if (mode == 1) {
                        result = result - i;
                    }
                    if (mode == 2) {
                        result = result * i;
                    }
                }
                intStorage.add(result);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(-13));
    }

    @Test
    public void floatArrayTest() {
        String code = """
                float[] a = new float[] { 0.5, 1.5, 2.5 };
                for (int i = 0; i < 3; i++) floatStorage.add(a[i]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                List.of(0.5, 1.5, 2.5));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
    }
}