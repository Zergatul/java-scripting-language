package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class WhileLoopTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void basicTest() {
        String code =
                "int i = 0;\n" +
                "while (i < 10) {\n" +
                "    intStorage.add(i);\n" +
                "    i++;\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    @Test
    public void continueBreakTest() {
        String code =
                "int i = 0;\n" +
                "int sum = 0;\n" +
                "while (true) {\n" +
                "    i++;\n" +
                "    if (i < 10) continue;\n" +
                "    if (i > 20) break;\n" +
                "    sum += i;\n" +
                "}\n" +
                "intStorage.add(sum);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(165));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}