package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class IsExpressionConstantPatternTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void nullTest() {
        String code =
                "string str1 = null;\n" +
                "boolStorage.add(str1 is null);\n" +
                "boolStorage.add(str1 is not null);\n" +
                "boolStorage.add(str1 is not not null);\n" +
                "boolStorage.add(str1 is not not not null);\n" +
                "string str2;\n" +
                "boolStorage.add(str2 is null);\n" +
                "boolStorage.add(str2 is not null);\n" +
                "boolStorage.add(str2 is not not null);\n" +
                "boolStorage.add(str2 is not not not null);\n" +
                "int x;\n" +
                "boolStorage.add(x is null);\n" +
                "boolStorage.add(x is not null);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(
                        true, false, true, false,
                        false, true, false, true,
                        false, true));
    }

    @Test
    public void boolLiteralTest() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "typealias Boolean = Java<java.lang.Boolean>;\n" +
                "\n" +
                "let b1 = false;\n" +
                "boolStorage.add(b1 is false);\n" +
                "boolStorage.add(b1 is not false);\n" +
                "boolStorage.add(b1 is not not false);\n" +
                "boolStorage.add(b1 is true);\n" +
                "boolStorage.add(b1 is not true);\n" +
                "boolStorage.add(b1 is not not true);\n" +
                "let b2 = true;\n" +
                "boolStorage.add(b2 is false);\n" +
                "boolStorage.add(b2 is not false);\n" +
                "boolStorage.add(b2 is not not false);\n" +
                "boolStorage.add(b2 is true);\n" +
                "boolStorage.add(b2 is not true);\n" +
                "boolStorage.add(b2 is not not true);\n" +
                "let str = \"\";\n" +
                "boolStorage.add(str is false);\n" +
                "boolStorage.add(str is not false);\n" +
                "boolStorage.add(str is true);\n" +
                "boolStorage.add(str is not true);\n" +
                "Object boxed1 = Boolean.FALSE;\n" +
                "boolStorage.add(boxed1 is false);\n" +
                "boolStorage.add(boxed1 is true);\n" +
                "Object boxed2 = Boolean.TRUE;\n" +
                "boolStorage.add(boxed2 is false);\n" +
                "boolStorage.add(boxed2 is true);\n" +
                "Object obj = null;\n" +
                "boolStorage.add(obj is false);\n" +
                "boolStorage.add(obj is true);\n" +
                "int x = 1;\n" +
                "boolStorage.add(x is false);\n" +
                "boolStorage.add(x is true);\n" +
                "boolStorage.add(x is not false);\n" +
                "boolStorage.add(x is not true);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(
                        true, false, true, false, true, false,
                        false, true, false, true, false, true,
                        false, true, false, true,
                        true, false,
                        false, true,
                        false, false,
                        false, false, true, true));
    }

    @Test
    public void int32LiteralTest() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "int i1 = 100;\n" +
                "boolStorage.add(i1 is 100);\n" +
                "boolStorage.add(i1 is not 99);\n" +
                "boolStorage.add(i1 is not 100);\n" +
                "\n" +
                "string str = \"\";\n" +
                "boolStorage.add(str is 100);\n" +
                "boolStorage.add(str is not 100);\n" +
                "\n" +
                "Object boxed = 50;\n" +
                "boolStorage.add(boxed is 50);\n" +
                "boolStorage.add(boxed is not 49);\n" +
                "boolStorage.add(boxed is not 50);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(
                        true, true, false,
                        false, true,
                        true, true, false));
    }

    @Test
    public void int64LiteralTest() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "long i1 = 100;\n" +
                "boolStorage.add(i1 is 100L);\n" +
                "boolStorage.add(i1 is not 99L);\n" +
                "boolStorage.add(i1 is not 100L);\n" +
                "\n" +
                "string str = \"\";\n" +
                "boolStorage.add(str is 100L);\n" +
                "boolStorage.add(str is not 100L);\n" +
                "\n" +
                "Object boxed = 50L;\n" +
                "boolStorage.add(boxed is 50L);\n" +
                "boolStorage.add(boxed is not 49L);\n" +
                "boolStorage.add(boxed is not 50L);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(
                        true, true, false,
                        false, true,
                        true, true, false));
    }

    @Test
    public void float64LiteralTest() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "float f1 = 100;\n" +
                "boolStorage.add(f1 is 100.0);\n" +
                "boolStorage.add(f1 is not 99.0);\n" +
                "boolStorage.add(f1 is not 100.0);\n" +
                "\n" +
                "string str = \"\";\n" +
                "boolStorage.add(str is 100.0);\n" +
                "boolStorage.add(str is not 100.0);\n" +
                "\n" +
                "Object boxed = 50.0;\n" +
                "boolStorage.add(boxed is 50.0);\n" +
                "boolStorage.add(boxed is not 49.0);\n" +
                "boolStorage.add(boxed is not 50.0);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(
                        true, true, false,
                        false, true,
                        true, true, false));
    }

    @Test
    public void stringLiteralTest() {}

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
    }
}