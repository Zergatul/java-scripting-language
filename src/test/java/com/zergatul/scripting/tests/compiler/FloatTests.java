package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class FloatTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void initialValueTest() {
        String code = """
                float f;
                floatStorage.add(f);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                List.of(0.0));
    }

    @Test
    public void initExpressionTest() {
        String code = """
                float f = 1.5 + 5.5;
                floatStorage.add(f);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                List.of(7.0));
    }

    @Test
    public void equalsOperatorTest() {
        String code = """
                boolStorage.add(0.5 == 0.5);
                boolStorage.add(1.5 + 1.5 == 0.5 + 2.5);
                boolStorage.add(0.5 == 0.6);
                boolStorage.add(1.5 + 1.6 == 0.5 + 2.5);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true, false, false));
    }

    @Test
    public void notEqualsOperatorTest() {
        String code = """
                boolStorage.add(0.5 != 0.5);
                boolStorage.add(1.5 + 1.5 != 0.5 + 2.5);
                boolStorage.add(0.5 != 0.6);
                boolStorage.add(1.5 + 1.6 != 0.5 + 2.5);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(false, false, true, true));
    }

    @Test
    public void lessThanOperatorTest() {
        String code = """
                boolStorage.add(10000.0 < 10001.0);
                boolStorage.add(10001.0 < 10000.0);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, false));
    }

    @Test
    public void greaterThanOperatorTest() {
        String code = """
                boolStorage.add(123456.0 > -1235456.0);
                boolStorage.add(-1235456.0 > 123456.0);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, false));
    }

    @Test
    public void lessThanEqualsOperatorTest() {
        String code = """
                boolStorage.add(1000000.0 <= 1000000.0);
                boolStorage.add(1000000.0 <= 1000001.0);
                boolStorage.add(1000000.0 <= 999999.0);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true, false));
    }

    @Test
    public void greaterThanEqualsOperatorTest() {
        String code = """
                boolStorage.add(1000000.0 >= 1000000.0);
                boolStorage.add(1000001.0 >= 1000000.0);
                boolStorage.add(999999.0 >= 1000000.0);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true, false));
    }

    @Test
    public void minusTest() {
        String code = """
                float f = -123;
                floatStorage.add(- -f);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                List.of(-123.0));
    }

    @Test
    public void implicitCastTest1() {
        String code = """
                floatStorage.add(1.5 + 1);
                floatStorage.add(1 + 1.5);
                floatStorage.add(1.5 - 1);
                floatStorage.add(2 - 1.5);
                floatStorage.add(1.5 * 2);
                floatStorage.add(1.2 / 2);
                floatStorage.add(1);
                
                boolStorage.add(1.5 > 1);
                boolStorage.add(1.5 < 1);
                boolStorage.add(1.5 >= 1);
                boolStorage.add(1.5 <= 1);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                List.of(2.5, 2.5, 0.5, 0.5, 3.0, 0.6, 1.0));
        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, false, true, false));
    }

    @Test
    public void implicitCastTest2() {
        String code = """
                boolStorage.add(1 + 0.5 == 1.5);
                boolStorage.add(0.5 + 1 == 1.5);
                boolStorage.add(1.5 - 0.5 == 1.0);
                boolStorage.add(1 - 0.5 == 0.5);
                
                boolStorage.add(1.5 * 2 == 3.0);
                boolStorage.add(2 * 1.5 == 3.0);
                boolStorage.add(3.0 / 2 == 1.5);
                boolStorage.add(3 / 2.0 == 1.5);
                boolStorage.add(3.0 % 2 == 1.0);
                boolStorage.add(3 % 2.0 == 1.0);
                
                boolStorage.add(1.9 < 2);
                boolStorage.add(2 < 2.1);
                boolStorage.add(2.1 > 2);
                boolStorage.add(2 > 1.9);
                
                boolStorage.add(1.9 <= 2);
                boolStorage.add(2.0 <= 2);
                boolStorage.add(2 <= 2.1);
                boolStorage.add(2 <= 2.0);
                boolStorage.add(2.1 >= 2);
                boolStorage.add(2.0 >= 2);
                boolStorage.add(2 >= 1.9);
                boolStorage.add(2 >= 2.0);
                
                boolStorage.add(2.0 == 2);
                boolStorage.add(2 == 2.0);
                boolStorage.add(2.1 != 2);
                boolStorage.add(2 != 2.1);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertEquals(ApiRoot.boolStorage.list.size(), 26);
        for (int i = 0; i < 26; i++) {
            Assertions.assertTrue(ApiRoot.boolStorage.list.get(i));
        }
    }

    @Test
    public void toStringTest() {
        String code = """
                float x = 100.25;
                stringStorage.add(x.toString());
                stringStorage.add((10.125).toString());
                
                stringStorage.add((10000.5555).toStandardString(3));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                List.of("100.25", "10.125", "10,000.556"));
    }

    @Test
    public void tryParseTest() {
        String code = """
                float x;
                boolStorage.add(float.tryParse("0.75", ref x));
                floatStorage.add(x);
                
                boolStorage.add(float.tryParse("0.0.0", ref x));
                floatStorage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, false));
        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                List.of(0.75, 0.75));
    }

    @Test
    public void leftAssociativityTest() {
        String code = """
                int x1 = 25;
                int x2 = 200;
                float percents = 100.0 * x1 / x2;
                floatStorage.add(percents);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, List.of(12.5));
    }

    @Test
    public void literalWithDotTest() {
        String code = """
                stringStorage.add(.5.toString());
                stringStorage.add(10.5.toString());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("0.5", "10.5"));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
    }
}