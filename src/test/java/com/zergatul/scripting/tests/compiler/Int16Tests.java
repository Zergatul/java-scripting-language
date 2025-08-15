package com.zergatul.scripting.tests.compiler;

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
        String code = """
                int16 i;
                int16Storage.add(i);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.int16Storage.list, List.of((short) 0));
    }

    @Test
    public void initExpressionTest() {
        String code = """
                int16 i = (123).toInt16();
                int16Storage.add(i);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.int16Storage.list, List.of((short) 123));
    }

    @Test
    public void addTest() {
        String code = """
                int16 i1 = (123).toInt16();
                int16 i2 = (456).toInt16();
                intStorage.add(i1 + i2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(579));
    }

    @Test
    public void subtractTest() {
        String code = """
                int16 i1 = (123).toInt16();
                int16 i2 = (456).toInt16();
                intStorage.add(i1 - i2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(-333));
    }

    @Test
    public void multiplyTest() {
        String code = """
                int16 i1 = (123).toInt16();
                int16 i2 = (456).toInt16();
                intStorage.add(i1 * i2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(56088));
    }

    @Test
    public void divisionTest() {
        String code = """
                int16 i1 = (1234).toInt16();
                int16 i2 = (56).toInt16();
                intStorage.add(i1 / i2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(22));
    }

    @Test
    public void moduloTest() {
        String code = """
                int16 i1 = (1234).toInt16();
                int16 i2 = (56).toInt16();
                intStorage.add(i1 % i2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(2));
    }

    @Test
    public void equalsOperatorTest() {
        String code = """
                int16 i1 = (123).toInt16();
                int16 i2 = (456).toInt16();
                int16 i3 = (456).toInt16();
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
                int16 i1 = (123).toInt16();
                int16 i2 = (456).toInt16();
                int16 i3 = (456).toInt16();
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
                boolStorage.add((10000).toInt16() < (10001).toInt16());
                boolStorage.add((10001).toInt16() < (10000).toInt16());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false));
    }

    @Test
    public void greaterThanOperatorTest() {
        String code = """
                boolStorage.add((1234).toInt16() > (-12345).toInt16());
                boolStorage.add((-12354).toInt16() > (1234).toInt16());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false));
    }

    @Test
    public void lessThanEqualsOperatorTest() {
        String code = """
                boolStorage.add((10000).toInt16() <= (10000).toInt16());
                boolStorage.add((10000).toInt16() <= (10001).toInt16());
                boolStorage.add((10000).toInt16() <= (9999).toInt16());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, true, false));
    }

    @Test
    public void greaterThanEqualsOperatorTest() {
        String code = """
                boolStorage.add((10000).toInt16() >= (10000).toInt16());
                boolStorage.add((10001).toInt16() >= (10000).toInt16());
                boolStorage.add((9999).toInt16() >= (10000).toInt16());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, true, false));
    }
    
    @Test
    public void minusTest() {
        String code = """
                intStorage.add(-(123).toInt16());
                intStorage.add(+(123).toInt16());
                intStorage.add(- -(123).toInt16());
                intStorage.add(- - -(123).toInt16());
                intStorage.add(- - - + + + + + + + + +  + + +(123).toInt16());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(-123, 123, 123, -123, -123));
    }

    @Test
    public void bitwiseTest() {
        String code = """
                intStorage.add(12345 | 8765);
                intStorage.add(12345 & 8765);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(12861, 8249));
    }

    @Test
    public void toStringTest() {
        String code = """
                stringStorage.add((500).toInt16().toString());
                stringStorage.add((1234).toInt16().toStandardString());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("500", "1,234"));
    }

    @Test
    public void tryParseTest() {
        String code = """
                int16 x;
                boolStorage.add(int16.tryParse("2010", ref x));
                int16Storage.add(x);
                
                boolStorage.add(int16.tryParse("a", ref x));
                int16Storage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false));
        Assertions.assertIterableEquals(ApiRoot.int16Storage.list, List.of((short) 2010, (short) 2010));
    }

    @Test
    public void castsTest() {
        String code = """
                int16 i16 = (123).toInt16();
                int32 i32 = i16;
                int64 i64 = i16;
                float32 f32 = i16;
                float64 f64 = i16;
                stringStorage.add(i32.toString());
                stringStorage.add(i64.toString());
                stringStorage.add(f32.toString());
                stringStorage.add(f64.toString());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("123", "123", "123.0", "123.0"));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static Int16Storage int16Storage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}