package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.tests.compiler.helpers.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class Int8Tests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.int8Storage = new Int8Storage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void initialValueTest() {
        String code =
                "int8 i;\n" +
                "int8Storage.add(i);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.int8Storage.list, Lists.of((byte) 0));
    }

    @Test
    public void initExpressionTest() {
        String code =
                "int8 i = (123).toInt8();\n" +
                "int8Storage.add(i);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.int8Storage.list, Lists.of((byte) 123));
    }

    @Test
    public void addTest() {
        String code =
                "int8 i1 = (123).toInt8();\n" +
                "int8 i2 = (112).toInt8();\n" +
                "intStorage.add(i1 + i2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(235));
    }

    @Test
    public void subtractTest() {
        String code =
                "int8 i1 = (123).toInt8();\n" +
                "int8 i2 = (-102).toInt8();\n" +
                "intStorage.add(i1 - i2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(225));
    }

    @Test
    public void multiplyTest() {
        String code =
                "int8 i1 = (123).toInt8();\n" +
                "int8 i2 = (102).toInt8();\n" +
                "intStorage.add(i1 * i2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(12546));
    }

    @Test
    public void divisionTest() {
        String code =
                "int8 i1 = (123).toInt8();\n" +
                "int8 i2 = (6).toInt8();\n" +
                "intStorage.add(i1 / i2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(20));
    }

    @Test
    public void moduloTest() {
        String code =
                "int8 i1 = (123).toInt8();\n" +
                "int8 i2 = (6).toInt8();\n" +
                "intStorage.add(i1 % i2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(3));
    }

    @Test
    public void equalsOperatorTest() {
        String code =
                "int8 i1 = (123).toInt8();\n" +
                "int8 i2 = (124).toInt8();\n" +
                "int8 i3 = (124).toInt8();\n" +
                "boolStorage.add(i1 == i2);\n" +
                "boolStorage.add(i2 == i3);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(false, true));
    }

    @Test
    public void notEqualsOperatorTest() {
        String code =
                "int8 i1 = (123).toInt8();\n" +
                "int8 i2 = (124).toInt8();\n" +
                "int8 i3 = (124).toInt8();\n" +
                "boolStorage.add(i1 != i2);\n" +
                "boolStorage.add(i2 != i3);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false));
    }

    @Test
    public void lessThanOperatorTest() {
        String code =
                "boolStorage.add((100).toInt8() < (101).toInt8());\n" +
                "boolStorage.add((101).toInt8() < (100).toInt8());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false));
    }

    @Test
    public void greaterThanOperatorTest() {
        String code =
                "boolStorage.add((12).toInt8() > (-123).toInt8());\n" +
                "boolStorage.add((-123).toInt8() > (124).toInt8());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false));
    }

    @Test
    public void lessThanEqualsOperatorTest() {
        String code =
                "boolStorage.add((100).toInt8() <= (100).toInt8());\n" +
                "boolStorage.add((100).toInt8() <= (101).toInt8());\n" +
                "boolStorage.add((100).toInt8() <= (99).toInt8());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, true, false));
    }

    @Test
    public void greaterThanEqualsOperatorTest() {
        String code =
                "boolStorage.add((100).toInt8() >= (100).toInt8());\n" +
                "boolStorage.add((101).toInt8() >= (100).toInt8());\n" +
                "boolStorage.add((99).toInt8() >= (100).toInt8());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, true, false));
    }
    
    @Test
    public void minusTest() {
        String code =
                "intStorage.add(-(123).toInt8());\n" +
                "intStorage.add(+(123).toInt8());\n" +
                "intStorage.add(- -(123).toInt8());\n" +
                "intStorage.add(- - -(123).toInt8());\n" +
                "intStorage.add(- - - + + + + + + + + +  + + +(123).toInt8());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(-123, 123, 123, -123, -123));
    }

    @Test
    public void bitwiseTest() {
        String code =
                "intStorage.add(123 | 87);\n" +
                "intStorage.add(123 & 87);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(127, 83));
    }

    @Test
    public void toStringTest() {
        String code =
                "stringStorage.add((123).toInt8().toString());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("123"));
    }

    @Test
    public void tryParseTest() {
        String code =
                "int8 x;\n" +
                "boolStorage.add(int8.tryParse(\"98\", ref x));\n" +
                "int8Storage.add(x);\n" +
                "\n" +
                "boolStorage.add(int8.tryParse(\"a\", ref x));\n" +
                "int8Storage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false));
        Assertions.assertIterableEquals(ApiRoot.int8Storage.list, Lists.of((byte) 98, (byte) 98));
    }

    @Test
    public void castsTest() {
        String code =
                "int8 i8 = (123).toInt8();\n" +
                "int16 i16 = i8;\n" +
                "int32 i32 = i8;\n" +
                "int64 i64 = i8;\n" +
                "float32 f32 = i8;\n" +
                "float64 f64 = i8;\n" +
                "stringStorage.add(i16.toString());\n" +
                "stringStorage.add(i32.toString());\n" +
                "stringStorage.add(i64.toString());\n" +
                "stringStorage.add(f32.toString());\n" +
                "stringStorage.add(f64.toString());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("123", "123", "123", "123.0", "123.0"));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static Int8Storage int8Storage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}