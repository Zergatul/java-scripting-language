package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class ReturnTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void simpleTest() {
        String code =
                "int a1 = 123;\n" +
                "int a2 = 456;\n" +
                "if (a1 > a2) {\n" +
                "    return;\n" +
                "}\n" +
                "intStorage.add(15);\n" +
                "if (a1 < a2) {\n" +
                "    return;\n" +
                "}\n" +
                "intStorage.add(16);\n" +
                "return;\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(15));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}