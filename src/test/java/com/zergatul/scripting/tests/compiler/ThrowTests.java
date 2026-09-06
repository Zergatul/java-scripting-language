package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class ThrowTests {

    @BeforeEach
    public void clean() {
        ApiRoot.storage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void basicTest() {
        String code =
                "typealias RuntimeException = Java<java.lang.RuntimeException>;\n" +
                "\n" +
                "throw new RuntimeException();\n";

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(RuntimeException.class, program::run);
    }

    @Test
    public void conditionalExpressionTest() {
        String code =
                "typealias RuntimeException = Java<java.lang.RuntimeException>;\n" +
                "\n" +
                "boolean b = true;\n" +
                "intStorage.add(b ? 100 : throw new RuntimeException());\n" +
                "intStorage.add(b ? throw new RuntimeException() : 200);\n";

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(RuntimeException.class, program::run);

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(100));
    }

    @Test
    public void arrowFunctionTest() {
        String code =
                "typealias RuntimeException = Java<java.lang.RuntimeException>;\n" +
                "\n" +
                "int func(int x1, int x2) => throw new RuntimeException();\n" +
                "\n" +
                "intStorage.add(func(1, 2));\n";

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(RuntimeException.class, program::run);
    }

    @Test
    public void lambdaTest() {
        String code =
                "typealias RuntimeException = Java<java.lang.RuntimeException>;\n" +
                "\n" +
                "void log(fn<() => int> func) => intStorage.add(func());\n" +
                "\n" +
                "log(() => throw new RuntimeException());\n";

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(RuntimeException.class, program::run);
    }

    @Test
    public void controlFlowTest1() {
        String code =
                "typealias RuntimeException = Java<java.lang.RuntimeException>;\n" +
                "\n" +
                "int func() {\n" +
                "    throw new RuntimeException();\n" +
                "}\n" +
                "\n" +
                "func();\n";

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(RuntimeException.class, program::run);
    }

    @Test
    public void controlFlowTest2() {
        String code =
                "typealias RuntimeException = Java<java.lang.RuntimeException>;\n" +
                "\n" +
                "int func(boolean b) {\n" +
                "    intStorage.add(b ? throw new RuntimeException(\"1\") : throw new RuntimeException(\"2\"));\n" +
                "}\n" +
                "\n" +
                "func(true);\n";

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(RuntimeException.class, program::run);
    }

    public static class ApiRoot {
        public static BoolStorage storage;
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
    }
}