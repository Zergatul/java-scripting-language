package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.Int64Storage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class Int64Tests {

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

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);

        Assertions.assertIterableEquals(
                messages,
                List.of(
                        new DiagnosticMessage(BinderErrors.IntegerConstantTooSmall, new SingleLineTextRange(2, 18, 58, 21))));
    }

    @Test
    public void constantTooLargeTest() {
        String code = """
                int64Storage.add(9223372036854775807L);
                int64Storage.add(9223372036854775808L);
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);

        Assertions.assertIterableEquals(
                messages,
                List.of(
                        new DiagnosticMessage(BinderErrors.IntegerConstantTooLarge, new SingleLineTextRange(2, 18, 57, 20))));
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

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);
        Assertions.assertIterableEquals(
                messages,
                List.of(new DiagnosticMessage(
                        BinderErrors.IntegerConstantTooLarge,
                        new SingleLineTextRange(1, 11, 10, 19))));
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

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static Int64Storage int64Storage;
        public static StringStorage stringStorage;
    }
}