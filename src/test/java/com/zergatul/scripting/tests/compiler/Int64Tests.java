package com.zergatul.scripting.tests.compiler;

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
        String code = """
                int64 i;
                int64Storage.add(i);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(0L));
    }

    @Test
    public void initExpressionTest() {
        String code = """
                int64 i = 9876543210L + 8765432190L;
                int64Storage.add(i);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(18641975400L));
    }

    @Test
    public void implicitConversionTest() {
        String code = """
                int a = 123456;
                int64 b = a;
                int64Storage.add(b);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(123456L));
    }

    @Test
    public void constantTooSmallTest() {
        String code = """
                int64Storage.add(-9223372036854775808L);
                int64Storage.add(-9223372036854775809L);
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(BinderErrors.IntegerConstantTooSmall, new SingleLineTextRange(2, 18, 58, 21))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void constantTooLargeTest() {
        String code = """
                int64Storage.add(9223372036854775807L);
                int64Storage.add(9223372036854775808L);
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(BinderErrors.IntegerConstantTooLarge, new SingleLineTextRange(2, 18, 57, 20))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void hexNumbers1Test() {
        String code = """
                int64Storage.add(0x0L);
                int64Storage.add(0x0000000000000000L);
                int64Storage.add(0x1L);
                int64Storage.add(0x01L);
                int64Storage.add(0x0000000000000001L);
                int64Storage.add(0xFFFFFFFFFFFFFFFFL);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(0L, 0L, 1L, 1L, 1L, 0xFFFFFFFFFFFFFFFFL));
    }

    @Test
    public void hexConstantTooLongTest() {
        String code = """
                int64 x = 0x1FFFFFFFFFFFFFFFF;
                int64Storage.add(x);
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(
                    BinderErrors.IntegerConstantTooLarge,
                    new SingleLineTextRange(1, 11, 10, 19))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void addTest() {
        String code = """
                int64Storage.add(123L + 1L);
                int64Storage.add(-123L + 123L);
                int64Storage.add(2000000000L + 2000000000L);
                int64Storage.add(2000000000 + 2000000000L);
                int64Storage.add(2000000000L + 2000000000);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(124L, 0L, 4000000000L, 4000000000L, 4000000000L));
    }

    @Test
    public void subtractTest() {
        String code = """
                int64Storage.add(123L - 500L);
                int64Storage.add(-123L - 123L);
                int64Storage.add(-4000000000L - 1000000000L);
                int64Storage.add(-2000000000 - 1000000000L);
                int64Storage.add(-4000000000L - 1000000000);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(-377L, -246L, -5000000000L, -3000000000L, -5000000000L));
    }

    @Test
    public void multiplyTest() {
        String code = """
                int64Storage.add(123L * -123L);
                int64Storage.add(-123L * -2L);
                int64Storage.add(123456789L * 987654321L);
                int64Storage.add(123456789 * 987654321L);
                int64Storage.add(123456789L * 987654321);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(-15129L, 246L, 121932631112635269L, 121932631112635269L, 121932631112635269L));
    }

    @Test
    public void divideTest() {
        String code = """
                int64Storage.add(123L / -123L);
                int64Storage.add(-123L / -2L);
                int64Storage.add(123123123123123L / 321321L);
                int64Storage.add(1231231231 / 321321L);
                int64Storage.add(1231231231L / 321321);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(-1L, 61L, 383177953L, 3831L, 3831L));
    }

    @Test
    public void moduloTest() {
        String code = """
                int64Storage.add(123L % -123L);
                int64Storage.add(-123L % -2L);
                int64Storage.add(123123123123123L % 321321L);
                int64Storage.add(1231231231 % 321321L);
                int64Storage.add(1231231231L % 321321);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(0L, -1L, 87210L, 250480L, 250480L));
    }

    @Test
    public void equalsOperatorTest() {
        String code = """
                boolStorage.add(12345L == 12345L);
                boolStorage.add(12345L == 12346L);
                boolStorage.add(-12345L == 12345L);
                boolStorage.add(12345L == -12345L);
                boolStorage.add(12300L + 45L == 12000L + 345L);
                boolStorage.add(1L == 0L);
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
                boolStorage.add(12345L != 12345L);
                boolStorage.add(12345L != 12346L);
                boolStorage.add(-12345L != 12345L);
                boolStorage.add(12345L != -12345L);
                boolStorage.add(12300L + 45L != 12000L + 345L);
                boolStorage.add(1L != 0L);
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
                boolStorage.add(10000L < 10001L);
                boolStorage.add(10001L < 10000L);
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
                boolStorage.add(123456L > -1235456L);
                boolStorage.add(-1235456L > 123456L);
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
                boolStorage.add(1000000L <= 1000000L);
                boolStorage.add(1000000L <= 1000001L);
                boolStorage.add(1000000L <= 999999L);
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
                boolStorage.add(1000000L >= 1000000L);
                boolStorage.add(1000001L >= 1000000L);
                boolStorage.add(999999L >= 1000000L);
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
                int64Storage.add(-123L);
                int64Storage.add(+123L);
                int64Storage.add(- -123L);
                int64Storage.add(- - -123L);
                int64Storage.add(- - - + + + + + + + + +  + + +123L);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(-123L, 123L, 123L, -123L, -123L));
    }

    @Test
    public void incrementTest() {
        String code = """
                int64 x;
                x++;
                int64Storage.add(x);
                x++;
                int64Storage.add(x);
                x++;
                int64Storage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(1L, 2L, 3L));
    }

    @Test
    public void decrementTest() {
        String code = """
                int64 x;
                x--;
                int64Storage.add(x);
                x--;
                int64Storage.add(x);
                x--;
                int64Storage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(-1L, -2L, -3L));
    }

    @Test
    public void bitwiseTest() {
        String code = """
                int64Storage.add(1234567812345678L | 8765432187654321L);
                int64Storage.add(1234567812345678L & 8765432187654321L);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(8838824591359999L, 1161175408640000L));
    }

    @Test
    public void augmentedAssignmentTest() {
        String code = """
                int64 x = 10;
                x += 5;
                int64Storage.add(x);
                
                x -= 10;
                int64Storage.add(x);
                
                x *= 6;
                int64Storage.add(x);
                
                x /= 3;
                int64Storage.add(x);
                
                x %= 3;
                int64Storage.add(x);
                
                x &= 13;
                int64Storage.add(x);
                
                x |= 12;
                int64Storage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(15L, 5L, 30L, 10L, 1L, 1L, 13L));
    }

    @Test
    public void toStringTest() {
        String code = """
                int64 x = 500;
                stringStorage.add(x.toString());
                stringStorage.add((400L).toString());
                
                stringStorage.add((123456789123456789L).toStandardString());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                List.of("500", "400", "123,456,789,123,456,789"));
    }

    @Test
    public void tryParseTest() {
        String code = """
                long x;
                boolStorage.add(long.tryParse("2010", ref x));
                int64Storage.add(x);
                
                boolStorage.add(int64.tryParse("a", ref x));
                int64Storage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, false));
        Assertions.assertIterableEquals(
                ApiRoot.int64Storage.list,
                List.of(2010L, 2010L));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static Int64Storage int64Storage;
        public static StringStorage stringStorage;
    }
}