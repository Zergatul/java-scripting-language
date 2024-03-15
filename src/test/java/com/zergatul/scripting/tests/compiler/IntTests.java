package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.helpers.BoolStorage;
import com.zergatul.scripting.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.*;

public class IntTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void initialValueTest() {
        String code = """
                int i;
                intStorage.add(i);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(0));
    }

    @Test
    public void initExpressionTest() {
        String code = """
                int i = 123 + 456;
                intStorage.add(i);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(579));
    }

    @Test
    public void addTest() {
        String code = """
                intStorage.add(123 + 1);
                intStorage.add(-123 + 123);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(124, 0));
    }

    @Test
    public void subtractTest() {
        String code = """
                intStorage.add(123 - 500);
                intStorage.add(-123 - 123);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(-377, -246));
    }

    @Test
    public void multiplyTest() {
        String code = """
                intStorage.add(123 * -123);
                intStorage.add(-123 * -2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(-15129, 246));
    }

    @Test
    public void divideTest() {
        String code = """
                intStorage.add(123 / -123);
                intStorage.add(-123 / -2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(-1, 61));
    }

    @Test
    public void moduloTest() {
        String code = """
                intStorage.add(123 % -123);
                intStorage.add(-123 % -2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(0, -1));
    }

    @Test
    public void equalsOperatorTest() {
        String code = """
                boolStorage.add(12345 == 12345);
                boolStorage.add(12345 == 12346);
                boolStorage.add(-12345 == 12345);
                boolStorage.add(12345 == -12345);
                boolStorage.add(12300 + 45 == 12000 + 345);
                boolStorage.add(1 == 0);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, false, false, false, true, false));
    }

    @Test
    public void notEqualsOperatorTest() {
        String code = """
                boolStorage.add(12345 != 12345);
                boolStorage.add(12345 != 12346);
                boolStorage.add(-12345 != 12345);
                boolStorage.add(12345 != -12345);
                boolStorage.add(12300 + 45 != 12000 + 345);
                boolStorage.add(1 != 0);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(false, true, true, true, false, true));
    }

    @Test
    public void lessThanOperatorTest() {
        String code = """
                boolStorage.add(10000 < 10001);
                boolStorage.add(10001 < 10000);
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
                boolStorage.add(123456 > -1235456);
                boolStorage.add(-1235456 > 123456);
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
                boolStorage.add(1000000 <= 1000000);
                boolStorage.add(1000000 <= 1000001);
                boolStorage.add(1000000 <= 999999);
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
                boolStorage.add(1000000 >= 1000000);
                boolStorage.add(1000001 >= 1000000);
                boolStorage.add(999999 >= 1000000);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true, false));
    }

    /*@Test
    public void floorDivTest() {
        String code = """
                boolStorage.add(123 !/ 10 == 12);
                boolStorage.add(-1 !/ 10 == -1);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true));
    }

    @Test
    public void floorModTest() {
        String code = """
                boolStorage.add(123 !% 10 == 3);
                boolStorage.add(-1 !% 10 == 9);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true));
    }*/

    @Test
    public void minusTest() {
        String code = """
                intStorage.add(-123);
                intStorage.add(+123);
                intStorage.add(--123);
                intStorage.add(---123);
                intStorage.add(---++++++++++++123);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(-123, 123, 123, -123, -123));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
    }
}