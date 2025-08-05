package com.zergatul.scripting.tests.compiler;

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
        String code = """
                int8 i;
                int8Storage.add(i);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.int8Storage.list, List.of((byte) 0));
    }

    @Test
    public void initExpressionTest() {
        String code = """
                int8 i = (123).toInt8();
                int8Storage.add(i);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.int8Storage.list, List.of((byte) 123));
    }

    @Test
    public void addTest() {
        String code = """
                int8 i1 = (123).toInt8();
                int8 i2 = (112).toInt8();
                intStorage.add(i1 + i2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(235));
    }

    @Test
    public void subtractTest() {
        String code = """
                int8 i1 = (123).toInt8();
                int8 i2 = (-102).toInt8();
                intStorage.add(i1 - i2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(225));
    }

    @Test
    public void multiplyTest() {
        String code = """
                int8 i1 = (123).toInt8();
                int8 i2 = (102).toInt8();
                intStorage.add(i1 * i2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(12546));
    }

    @Test
    public void divisionTest() {
        String code = """
                int8 i1 = (123).toInt8();
                int8 i2 = (6).toInt8();
                intStorage.add(i1 / i2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(20));
    }

    @Test
    public void moduloTest() {
        String code = """
                int8 i1 = (123).toInt8();
                int8 i2 = (6).toInt8();
                intStorage.add(i1 % i2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(3));
    }

    @Test
    public void equalsOperatorTest() {
        String code = """
                int8 i1 = (123).toInt8();
                int8 i2 = (124).toInt8();
                int8 i3 = (124).toInt8();
                boolStorage.add(i1 == i2);
                boolStorage.add(i2 == i3);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(false, true));
    }

    @Test
    public void notEqualsOperatorTest() {
        String code = """
                int8 i1 = (123).toInt8();
                int8 i2 = (124).toInt8();
                int8 i3 = (124).toInt8();
                boolStorage.add(i1 != i2);
                boolStorage.add(i2 != i3);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false));
    }

    @Test
    public void lessThanOperatorTest() {
        String code = """
                boolStorage.add((100).toInt8() < (101).toInt8());
                boolStorage.add((101).toInt8() < (100).toInt8());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false));
    }

    @Test
    public void greaterThanOperatorTest() {
        String code = """
                boolStorage.add((12).toInt8() > (-123).toInt8());
                boolStorage.add((-123).toInt8() > (124).toInt8());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false));
    }

    @Test
    public void lessThanEqualsOperatorTest() {
        String code = """
                boolStorage.add((100).toInt8() <= (100).toInt8());
                boolStorage.add((100).toInt8() <= (101).toInt8());
                boolStorage.add((100).toInt8() <= (99).toInt8());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, true, false));
    }

    @Test
    public void greaterThanEqualsOperatorTest() {
        String code = """
                boolStorage.add((100).toInt8() >= (100).toInt8());
                boolStorage.add((101).toInt8() >= (100).toInt8());
                boolStorage.add((99).toInt8() >= (100).toInt8());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, true, false));
    }
    
    @Test
    public void minusTest() {
        String code = """
                intStorage.add(-(123).toInt8());
                intStorage.add(+(123).toInt8());
                intStorage.add(- -(123).toInt8());
                intStorage.add(- - -(123).toInt8());
                intStorage.add(- - - + + + + + + + + +  + + +(123).toInt8());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(-123, 123, 123, -123, -123));
    }

    @Test
    public void bitwiseTest() {
        String code = """
                intStorage.add(123 | 87);
                intStorage.add(123 & 87);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(127, 83));
    }

    @Test
    public void toStringTest() {
        String code = """
                stringStorage.add((123).toInt8().toString());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("123"));
    }

    @Test
    public void tryParseTest() {
        String code = """
                int8 x;
                boolStorage.add(int8.tryParse("98", ref x));
                int8Storage.add(x);
                
                boolStorage.add(int8.tryParse("a", ref x));
                int8Storage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false));
        Assertions.assertIterableEquals(ApiRoot.int8Storage.list, List.of((byte) 98, (byte) 98));
    }

    @Test
    public void castsTest() {
        String code = """
                int8 i8 = (123).toInt8();
                int16 i16 = i8;
                int32 i32 = i8;
                int64 i64 = i8;
                float32 f32 = i8;
                float64 f64 = i8;
                stringStorage.add(i16.toString());
                stringStorage.add(i32.toString());
                stringStorage.add(i64.toString());
                stringStorage.add(f32.toString());
                stringStorage.add(f64.toString());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("123", "123", "123", "123.0", "123.0"));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static Int8Storage int8Storage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}