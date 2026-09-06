package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class IfStatementTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void nestedIfTest1() {
        String code =
                "static boolean b1;\n" +
                "static boolean b2;\n" +
                "\n" +
                "void test() {\n" +
                "    if (b1)\n" +
                "        if (b2)\n" +
                "            intStorage.add(11);\n" +
                "        else\n" +
                "            intStorage.add(12);\n" +
                "    else\n" +
                "        if (b2)\n" +
                "            intStorage.add(13);\n" +
                "        else\n" +
                "            intStorage.add(14);\n" +
                "}\n" +
                "\n" +
                "b1 = false; b2 = false;\n" +
                "test();\n" +
                "intStorage.add(0);\n" +
                "\n" +
                "b1 = false; b2 = true;\n" +
                "test();\n" +
                "intStorage.add(0);\n" +
                "\n" +
                "b1 = true; b2 = false;\n" +
                "test();\n" +
                "intStorage.add(0);\n" +
                "\n" +
                "b1 = true; b2 = true;\n" +
                "test();\n" +
                "intStorage.add(0);\n" +
                "\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(14, 0, 13, 0, 12, 0, 11, 0));
    }

    @Test
    public void nestedIfTest2() {
        String code =
                "static boolean b1;\n" +
                "static boolean b2;\n" +
                "\n" +
                "void test() {\n" +
                "    if (b1) if (b2) intStorage.add(1); else intStorage.add(2);\n" +
                "}\n" +
                "\n" +
                "b1 = false; b2 = false;\n" +
                "test();\n" +
                "intStorage.add(0);\n" +
                "\n" +
                "b1 = false; b2 = true;\n" +
                "test();\n" +
                "intStorage.add(0);\n" +
                "\n" +
                "b1 = true; b2 = false;\n" +
                "test();\n" +
                "intStorage.add(0);\n" +
                "\n" +
                "b1 = true; b2 = true;\n" +
                "test();\n" +
                "intStorage.add(0);\n" +
                "\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(
                0,
                0,
                2, 0,
                1, 0));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}