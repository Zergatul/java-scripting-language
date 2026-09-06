package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.Int16Storage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class Int16Tests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.int16Storage = new Int16Storage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void initialValueTest() {
        String code =
                "int16 i;\n" +
                "int16Storage.add(i);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.int16Storage.list, Lists.of((short) 0));
    }

    @Test
    public void initExpressionTest() {
        String code =
                "int16 i = (123).toInt16();\n" +
                "int16Storage.add(i);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.int16Storage.list, Lists.of((short) 123));
    }

    @Test
    public void addTest() {
        String code =
                "int16 i1 = (123).toInt16();\n" +
                "int16 i2 = (456).toInt16();\n" +
                "intStorage.add(i1 + i2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(579));
    }

    @Test
    public void subtractTest() {
        String code =
                "int16 i1 = (123).toInt16();\n" +
                "int16 i2 = (456).toInt16();\n" +
                "intStorage.add(i1 - i2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(-333));
    }

    @Test
    public void multiplyTest() {
        String code =
                "int16 i1 = (123).toInt16();\n" +
                "int16 i2 = (456).toInt16();\n" +
                "intStorage.add(i1 * i2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(56088));
    }

    @Test
    public void divisionTest() {
        String code =
                "int16 i1 = (1234).toInt16();\n" +
                "int16 i2 = (56).toInt16();\n" +
                "intStorage.add(i1 / i2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(22));
    }

    @Test
    public void moduloTest() {
        String code =
                "int16 i1 = (1234).toInt16();\n" +
                "int16 i2 = (56).toInt16();\n" +
                "intStorage.add(i1 % i2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(2));
    }

    @Test
    public void equalsOperatorTest() {
        String code =
                "int16 i1 = (123).toInt16();\n" +
                "int16 i2 = (456).toInt16();\n" +
                "int16 i3 = (456).toInt16();\n" +
                "boolStorage.add(i1 == i2);\n" +
                "boolStorage.add(i2 == i3);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(false, true));
    }

    @Test
    public void notEqualsOperatorTest() {
        String code =
                "int16 i1 = (123).toInt16();\n" +
                "int16 i2 = (456).toInt16();\n" +
                "int16 i3 = (456).toInt16();\n" +
                "boolStorage.add(i1 != i2);\n" +
                "boolStorage.add(i2 != i3);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false));
    }

    @Test
    public void lessThanOperatorTest() {
        String code =
                "boolStorage.add((10000).toInt16() < (10001).toInt16());\n" +
                "boolStorage.add((10001).toInt16() < (10000).toInt16());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false));
    }

    @Test
    public void greaterThanOperatorTest() {
        String code =
                "boolStorage.add((1234).toInt16() > (-12345).toInt16());\n" +
                "boolStorage.add((-12354).toInt16() > (1234).toInt16());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false));
    }

    @Test
    public void lessThanEqualsOperatorTest() {
        String code =
                "boolStorage.add((10000).toInt16() <= (10000).toInt16());\n" +
                "boolStorage.add((10000).toInt16() <= (10001).toInt16());\n" +
                "boolStorage.add((10000).toInt16() <= (9999).toInt16());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, true, false));
    }

    @Test
    public void greaterThanEqualsOperatorTest() {
        String code =
                "boolStorage.add((10000).toInt16() >= (10000).toInt16());\n" +
                "boolStorage.add((10001).toInt16() >= (10000).toInt16());\n" +
                "boolStorage.add((9999).toInt16() >= (10000).toInt16());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, true, false));
    }
    
    @Test
    public void minusTest() {
        String code =
                "intStorage.add(-(123).toInt16());\n" +
                "intStorage.add(+(123).toInt16());\n" +
                "intStorage.add(- -(123).toInt16());\n" +
                "intStorage.add(- - -(123).toInt16());\n" +
                "intStorage.add(- - - + + + + + + + + +  + + +(123).toInt16());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(-123, 123, 123, -123, -123));
    }

    @Test
    public void bitwiseTest() {
        String code =
                "intStorage.add(12345 | 8765);\n" +
                "intStorage.add(12345 & 8765);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(12861, 8249));
    }

    @Test
    public void toStringTest() {
        String code =
                "stringStorage.add((500).toInt16().toString());\n" +
                "stringStorage.add((1234).toInt16().toStandardString());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("500", "1,234"));
    }

    @Test
    public void tryParseTest() {
        String code =
                "int16 x;\n" +
                "boolStorage.add(int16.tryParse(\"2010\", ref x));\n" +
                "int16Storage.add(x);\n" +
                "\n" +
                "boolStorage.add(int16.tryParse(\"a\", ref x));\n" +
                "int16Storage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false));
        Assertions.assertIterableEquals(ApiRoot.int16Storage.list, Lists.of((short) 2010, (short) 2010));
    }

    @Test
    public void castsTest() {
        String code =
                "int16 i16 = (123).toInt16();\n" +
                "int32 i32 = i16;\n" +
                "int64 i64 = i16;\n" +
                "float32 f32 = i16;\n" +
                "float64 f64 = i16;\n" +
                "stringStorage.add(i32.toString());\n" +
                "stringStorage.add(i64.toString());\n" +
                "stringStorage.add(f32.toString());\n" +
                "stringStorage.add(f64.toString());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("123", "123", "123.0", "123.0"));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static Int16Storage int16Storage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}