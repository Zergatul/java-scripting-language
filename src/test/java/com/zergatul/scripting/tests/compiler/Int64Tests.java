package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.Int64Storage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class Int64Tests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.int64Storage = new Int64Storage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void initialValueTest() {
        String code =
                "int64 i;\n" +
                "int64Storage.add(i);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(0L));
    }

    @Test
    public void initExpressionTest() {
        String code =
                "int64 i = 9876543210L + 8765432190L;\n" +
                "int64Storage.add(i);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(18641975400L));
    }

    @Test
    public void implicitConversionTest() {
        String code =
                "int a = 123456;\n" +
                "int64 b = a;\n" +
                "int64Storage.add(b);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(123456L));
    }

    @Test
    public void constantTooSmallTest() {
        String code =
                "int64Storage.add(-9223372036854775808L);\n" +
                "int64Storage.add(-9223372036854775809L);\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.IntegerConstantTooSmall, new SingleLineTextRange(2, 18, 58, 21))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void constantTooLargeTest() {
        String code =
                "int64Storage.add(9223372036854775807L);\n" +
                "int64Storage.add(9223372036854775808L);\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.IntegerConstantTooLarge, new SingleLineTextRange(2, 18, 57, 20))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void hexNumbers1Test() {
        String code =
                "int64Storage.add(0x0L);\n" +
                "int64Storage.add(0x0000000000000000L);\n" +
                "int64Storage.add(0x1L);\n" +
                "int64Storage.add(0x01L);\n" +
                "int64Storage.add(0x0000000000000001L);\n" +
                "int64Storage.add(0xFFFFFFFFFFFFFFFFL);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(0L, 0L, 1L, 1L, 1L, 0xFFFFFFFFFFFFFFFFL));
    }

    @Test
    public void hexConstantTooLongTest() {
        String code =
                "int64 x = 0x1FFFFFFFFFFFFFFFF;\n" +
                "int64Storage.add(x);\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(
                    BinderErrors.IntegerConstantTooLarge,
                    new SingleLineTextRange(1, 11, 10, 19))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void addTest() {
        String code =
                "int64Storage.add(123L + 1L);\n" +
                "int64Storage.add(-123L + 123L);\n" +
                "int64Storage.add(2000000000L + 2000000000L);\n" +
                "int64Storage.add(2000000000 + 2000000000L);\n" +
                "int64Storage.add(2000000000L + 2000000000);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(124L, 0L, 4000000000L, 4000000000L, 4000000000L));
    }

    @Test
    public void subtractTest() {
        String code =
                "int64Storage.add(123L - 500L);\n" +
                "int64Storage.add(-123L - 123L);\n" +
                "int64Storage.add(-4000000000L - 1000000000L);\n" +
                "int64Storage.add(-2000000000 - 1000000000L);\n" +
                "int64Storage.add(-4000000000L - 1000000000);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(-377L, -246L, -5000000000L, -3000000000L, -5000000000L));
    }

    @Test
    public void multiplyTest() {
        String code =
                "int64Storage.add(123L * -123L);\n" +
                "int64Storage.add(-123L * -2L);\n" +
                "int64Storage.add(123456789L * 987654321L);\n" +
                "int64Storage.add(123456789 * 987654321L);\n" +
                "int64Storage.add(123456789L * 987654321);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(-15129L, 246L, 121932631112635269L, 121932631112635269L, 121932631112635269L));
    }

    @Test
    public void divideTest() {
        String code =
                "int64Storage.add(123L / -123L);\n" +
                "int64Storage.add(-123L / -2L);\n" +
                "int64Storage.add(123123123123123L / 321321L);\n" +
                "int64Storage.add(1231231231 / 321321L);\n" +
                "int64Storage.add(1231231231L / 321321);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(-1L, 61L, 383177953L, 3831L, 3831L));
    }

    @Test
    public void moduloTest() {
        String code =
                "int64Storage.add(123L % -123L);\n" +
                "int64Storage.add(-123L % -2L);\n" +
                "int64Storage.add(123123123123123L % 321321L);\n" +
                "int64Storage.add(1231231231 % 321321L);\n" +
                "int64Storage.add(1231231231L % 321321);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(0L, -1L, 87210L, 250480L, 250480L));
    }

    @Test
    public void equalsOperatorTest() {
        String code =
                "boolStorage.add(12345L == 12345L);\n" +
                "boolStorage.add(12345L == 12346L);\n" +
                "boolStorage.add(-12345L == 12345L);\n" +
                "boolStorage.add(12345L == -12345L);\n" +
                "boolStorage.add(12300L + 45L == 12000L + 345L);\n" +
                "boolStorage.add(1L == 0L);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, false, false, false, true, false));
    }

    @Test
    public void notEqualsOperatorTest() {
        String code =
                "boolStorage.add(12345L != 12345L);\n" +
                "boolStorage.add(12345L != 12346L);\n" +
                "boolStorage.add(-12345L != 12345L);\n" +
                "boolStorage.add(12345L != -12345L);\n" +
                "boolStorage.add(12300L + 45L != 12000L + 345L);\n" +
                "boolStorage.add(1L != 0L);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(false, true, true, true, false, true));
    }

    @Test
    public void lessThanOperatorTest() {
        String code =
                "boolStorage.add(10000L < 10001L);\n" +
                "boolStorage.add(10001L < 10000L);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, false));
    }

    @Test
    public void greaterThanOperatorTest() {
        String code =
                "boolStorage.add(123456L > -1235456L);\n" +
                "boolStorage.add(-1235456L > 123456L);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, false));
    }

    @Test
    public void lessThanEqualsOperatorTest() {
        String code =
                "boolStorage.add(1000000L <= 1000000L);\n" +
                "boolStorage.add(1000000L <= 1000001L);\n" +
                "boolStorage.add(1000000L <= 999999L);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, true, false));
    }

    @Test
    public void greaterThanEqualsOperatorTest() {
        String code =
                "boolStorage.add(1000000L >= 1000000L);\n" +
                "boolStorage.add(1000001L >= 1000000L);\n" +
                "boolStorage.add(999999L >= 1000000L);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, true, false));
    }

    @Test
    public void minusTest() {
        String code =
                "int64Storage.add(-123L);\n" +
                "int64Storage.add(+123L);\n" +
                "int64Storage.add(- -123L);\n" +
                "int64Storage.add(- - -123L);\n" +
                "int64Storage.add(- - - + + + + + + + + +  + + +123L);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(-123L, 123L, 123L, -123L, -123L));
    }

    @Test
    public void incrementTest() {
        String code =
                "int64 x;\n" +
                "x++;\n" +
                "int64Storage.add(x);\n" +
                "x++;\n" +
                "int64Storage.add(x);\n" +
                "x++;\n" +
                "int64Storage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(1L, 2L, 3L));
    }

    @Test
    public void decrementTest() {
        String code =
                "int64 x;\n" +
                "x--;\n" +
                "int64Storage.add(x);\n" +
                "x--;\n" +
                "int64Storage.add(x);\n" +
                "x--;\n" +
                "int64Storage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(-1L, -2L, -3L));
    }

    @Test
    public void bitwiseTest() {
        String code =
                "int64Storage.add(1234567812345678L | 8765432187654321L);\n" +
                "int64Storage.add(1234567812345678L & 8765432187654321L);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(8838824591359999L, 1161175408640000L));
    }

    @Test
    public void augmentedAssignmentTest() {
        String code =
                "int64 x = 10;\n" +
                "x += 5;\n" +
                "int64Storage.add(x);\n" +
                "\n" +
                "x -= 10;\n" +
                "int64Storage.add(x);\n" +
                "\n" +
                "x *= 6;\n" +
                "int64Storage.add(x);\n" +
                "\n" +
                "x /= 3;\n" +
                "int64Storage.add(x);\n" +
                "\n" +
                "x %= 3;\n" +
                "int64Storage.add(x);\n" +
                "\n" +
                "x &= 13;\n" +
                "int64Storage.add(x);\n" +
                "\n" +
                "x |= 12;\n" +
                "int64Storage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(15L, 5L, 30L, 10L, 1L, 1L, 13L));
    }

    @Test
    public void toStringTest() {
        String code =
                "int64 x = 500;\n" +
                "stringStorage.add(x.toString());\n" +
                "stringStorage.add((400L).toString());\n" +
                "\n" +
                "stringStorage.add((123456789123456789L).toStandardString());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                Lists.of("500", "400", "123,456,789,123,456,789"));
    }

    @Test
    public void tryParseTest() {
        String code =
                "long x;\n" +
                "boolStorage.add(long.tryParse(\"2010\", ref x));\n" +
                "int64Storage.add(x);\n" +
                "\n" +
                "boolStorage.add(int64.tryParse(\"a\", ref x));\n" +
                "int64Storage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, false));
        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                Lists.of(2010L, 2010L));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static Int64Storage int64Storage;
        public static StringStorage stringStorage;
    }
}