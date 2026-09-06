package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

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
        String code =
                "intStorage.add(1);\n" +
                "try {\n" +
                "    intStorage.add(2);\n" +
                "    try {\n" +
                "        intStorage.add(3);\n" +
                "        (new int[0])[1] = 10; // throws\n" +
                "        intStorage.add(999);\n" +
                "    } finally {\n" +
                "        intStorage.add(4);\n" +
                "    }\n" +
                "    intStorage.add(9999);\n" +
                "} catch {\n" +
                "    intStorage.add(5);\n" +
                "} finally {\n" +
                "    intStorage.add(6);\n" +
                "}\n" +
                "intStorage.add(7);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5, 6, 7), ApiRoot.intStorage.list);
    }

    @Test
    public void nestedRethrowCaughtByOuterCatchTest() {
        String code =
                "intStorage.add(1);\n" +
                "try {\n" +
                "    try {\n" +
                "        intStorage.add(2);\n" +
                "        [1][2] = 3; // throws\n" +
                "    } catch {\n" +
                "        intStorage.add(3);\n" +
                "        throw;\n" +
                "    } finally {\n" +
                "        intStorage.add(4);\n" +
                "    }\n" +
                "    intStorage.add(999);\n" +
                "} catch {\n" +
                "    intStorage.add(5);\n" +
                "} finally {\n" +
                "    intStorage.add(6);\n" +
                "}\n" +
                "intStorage.add(7);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4, 5, 6, 7), ApiRoot.intStorage.list);
    }

    @Test
    public void nestedTryFinallyWithOuterLoopBreakTest() {
        String code =
                "intStorage.add(1);\n" +
                "foreach (int x in [2, 3, 4]) {\n" +
                "    try {\n" +
                "        try {\n" +
                "            intStorage.add(x);\n" +
                "            if (x == 3) {\n" +
                "                break;\n" +
                "            }\n" +
                "        } finally {\n" +
                "            intStorage.add(100 + x);\n" +
                "        }\n" +
                "    } finally {\n" +
                "        intStorage.add(200 + x);\n" +
                "    }\n" +
                "}\n" +
                "intStorage.add(9);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2, 102, 202, 3, 103, 203, 9), ApiRoot.intStorage.list);
    }

    @Test
    public void nestedFinallyAndReturnTest() {
        String code =
                "try {\n" +
                "    intStorage.add(1);\n" +
                "    try {\n" +
                "        intStorage.add(2);\n" +
                "        return;\n" +
                "    } finally {\n" +
                "        intStorage.add(3);\n" +
                "    }\n" +
                "} finally {\n" +
                "    intStorage.add(4);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(1, 2, 3, 4), ApiRoot.intStorage.list);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}