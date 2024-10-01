package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.*;

public class Int32Tests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
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
    public void constantTooSmallTest() {
        String code = """
                intStorage.add(-2147483648);
                intStorage.add(-2147483649);
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);

        Assertions.assertIterableEquals(
                messages,
                List.of(
                        new DiagnosticMessage(BinderErrors.IntegerConstantTooSmall, new SingleLineTextRange(2, 16, 44, 11))));
    }

    @Test
    public void constantTooLargeTest() {
        String code = """
                intStorage.add(2147483647);
                intStorage.add(2147483648);
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);

        Assertions.assertIterableEquals(
                messages,
                List.of(
                        new DiagnosticMessage(BinderErrors.IntegerConstantTooLarge, new SingleLineTextRange(2, 16, 43, 10))));
    }

    @Test
    public void hexNumbers1Test() {
        String code = """
                intStorage.add(0x0);
                intStorage.add(0x00000000);
                intStorage.add(0x00000001);
                intStorage.add(0x0000000a);
                intStorage.add(0x0000000F);
                intStorage.add(0x100);
                intStorage.add(0x7FFFFFFF);
                intStorage.add(0x8FFFFFFF);
                intStorage.add(0xFFFFFFFF);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(0, 0, 1, 0xA, 0xF, 0x100, 0x7FFFFFFF, 0x8FFFFFFF, 0xFFFFFFFF));
    }

    @Test
    public void hexNumbers2Test() {
        String code = """
                intStorage.add(0x000000000);
                intStorage.add(0x100000000);
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);

        Assertions.assertIterableEquals(
                messages,
                List.of(
                        new DiagnosticMessage(BinderErrors.IntegerConstantTooLarge, new SingleLineTextRange(1, 16, 15, 11)),
                        new DiagnosticMessage(BinderErrors.IntegerConstantTooLarge, new SingleLineTextRange(2, 16, 44, 11))));
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
                intStorage.add(- -123);
                intStorage.add(- - -123);
                intStorage.add(- - - + + + + + + + + +  + + +123);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(-123, 123, 123, -123, -123));
    }

    @Test
    public void incrementTest() {
        String code = """
                int x;
                x++;
                intStorage.add(x);
                x++;
                intStorage.add(x);
                x++;
                intStorage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1, 2, 3));
    }

    @Test
    public void decrementTest() {
        String code = """
                int x;
                x--;
                intStorage.add(x);
                x--;
                intStorage.add(x);
                x--;
                intStorage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(-1, -2, -3));
    }

    @Test
    public void bitwiseTest() {
        String code = """
                intStorage.add(12345678 | 87654321);
                intStorage.add(12345678 & 87654321);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(96305151, 3694848));
    }

    @Test
    public void augmentedAssignmentTest() {
        String code = """
                int x = 10;
                x += 5;
                intStorage.add(x);
                
                x -= 10;
                intStorage.add(x);
                
                x *= 6;
                intStorage.add(x);
                
                x /= 3;
                intStorage.add(x);
                
                x %= 3;
                intStorage.add(x);
                
                x &= 13;
                intStorage.add(x);
                
                x |= 12;
                intStorage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(15, 5, 30, 10, 1, 1, 13));
    }

    @Test
    public void toStringTest() {
        String code = """
                int x = 500;
                stringStorage.add(x.toString());
                stringStorage.add((400).toString());
                
                stringStorage.add((123456789).toStandardString());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                List.of("500", "400", "123,456,789"));
    }

    @Test
    public void tryParseTest() {
        String code = """
                int x;
                boolStorage.add(int.tryParse("2010", ref x));
                intStorage.add(x);
                
                boolStorage.add(int.tryParse("a", ref x));
                intStorage.add(x);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, false));
        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(2010, 2010));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}