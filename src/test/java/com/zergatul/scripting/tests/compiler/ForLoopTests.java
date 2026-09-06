package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

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
        String code =
                "for (;;) {\n" +
                "    intStorage.add(10);\n" +
                "    break;\n" +
                "    intStorage.add(20);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(10));
    }

    @Test
    public void continueStatementTest() {
        String code =
                "int sum;\n" +
                "for (int i = 10; i >= 0; i--) {\n" +
                "    if (i > 5) {\n" +
                "        continue;\n" +
                "    }\n" +
                "    sum = sum + i;\n" +
                "}\n" +
                "intStorage.add(sum);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(15));
    }

    @Test
    public void loopArrayAssignTest() {
        String code =
                "int[] a = new int[10];\n" +
                "for (int i = 0; i < a.length; i++) {\n" +
                "    a[i] = i + 1;\n" +
                "}\n" +
                "for (int i = 0; i < a.length; i++) {\n" +
                "    intStorage.add(a[i]);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void noInitializerTest() {
        String code =
                "int sum = 0;\n" +
                "int j = 0;\n" +
                "for (; j <= 10;) {\n" +
                "    sum = sum + j;\n" +
                "    j = j + 1;\n" +
                "}\n" +
                "intStorage.add(sum);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(55));
    }

    @Test
    public void backwardsTest() {
        String code =
                "int result = 1;\n" +
                "for (int i = 10; i >= 0; i--) {\n" +
                "    int mode = i % 3;\n" +
                "    if (mode == 0) {\n" +
                "        result = result + i;\n" +
                "    }\n" +
                "    if (mode == 1) {\n" +
                "        result = result - i;\n" +
                "    }\n" +
                "    if (mode == 2) {\n" +
                "        result = result * i;\n" +
                "    }\n" +
                "}\n" +
                "intStorage.add(result);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(-13));
    }

    @Test
    public void floatArrayTest() {
        String code =
                "float[] a = new float[] { 0.5, 1.5, 2.5 };\n" +
                "for (int i = 0; i < 3; i++) floatStorage.add(a[i]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                Lists.of(0.5, 1.5, 2.5));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
    }
}