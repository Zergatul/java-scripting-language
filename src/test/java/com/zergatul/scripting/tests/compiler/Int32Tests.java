package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.*;

public class Int32Tests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void initialValueTest() {
        String code =
                "int i;\n" +
                "intStorage.add(i);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(0));
    }

    @Test
    public void initExpressionTest() {
        String code =
                "int i = 123 + 456;\n" +
                "intStorage.add(i);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(579));
    }

    @Test
    public void constantTooSmallTest() {
        String code =
                "intStorage.add(-2147483648);\n" +
                "intStorage.add(-2147483649);\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.IntegerConstantTooSmall, new SingleLineTextRange(2, 16, 44, 11))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void constantTooLargeTest() {
        String code =
                "intStorage.add(2147483647);\n" +
                "intStorage.add(2147483648);\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.IntegerConstantTooLarge, new SingleLineTextRange(2, 16, 43, 10))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void hexNumbers1Test() {
        String code =
                "intStorage.add(0x0);\n" +
                "intStorage.add(0x00000000);\n" +
                "intStorage.add(0x00000001);\n" +
                "intStorage.add(0x0000000a);\n" +
                "intStorage.add(0x0000000F);\n" +
                "intStorage.add(0x100);\n" +
                "intStorage.add(0x7FFFFFFF);\n" +
                "intStorage.add(0x8FFFFFFF);\n" +
                "intStorage.add(0xFFFFFFFF);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(0, 0, 1, 0xA, 0xF, 0x100, 0x7FFFFFFF, 0x8FFFFFFF, 0xFFFFFFFF));
    }

    @Test
    public void hexNumbers2Test() {
        String code =
                "intStorage.add(0x000000000);\n" +
                "intStorage.add(0x100000000);\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.IntegerConstantTooLarge, new SingleLineTextRange(1, 16, 15, 11)),
                new DiagnosticMessage(BinderErrors.IntegerConstantTooLarge, new SingleLineTextRange(2, 16, 44, 11))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void addTest() {
        String code =
                "intStorage.add(123 + 1);\n" +
                "intStorage.add(-123 + 123);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(124, 0));
    }

    @Test
    public void subtractTest() {
        String code =
                "intStorage.add(123 - 500);\n" +
                "intStorage.add(-123 - 123);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(-377, -246));
    }

    @Test
    public void multiplyTest() {
        String code =
                "intStorage.add(123 * -123);\n" +
                "intStorage.add(-123 * -2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(-15129, 246));
    }

    @Test
    public void divideTest() {
        String code =
                "intStorage.add(123 / -123);\n" +
                "intStorage.add(-123 / -2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(-1, 61));
    }

    @Test
    public void moduloTest() {
        String code =
                "intStorage.add(123 % -123);\n" +
                "intStorage.add(-123 % -2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(0, -1));
    }

    @Test
    public void equalsOperatorTest() {
        String code =
                "boolStorage.add(12345 == 12345);\n" +
                "boolStorage.add(12345 == 12346);\n" +
                "boolStorage.add(-12345 == 12345);\n" +
                "boolStorage.add(12345 == -12345);\n" +
                "boolStorage.add(12300 + 45 == 12000 + 345);\n" +
                "boolStorage.add(1 == 0);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, false, false, false, true, false));
    }

    @Test
    public void notEqualsOperatorTest() {
        String code =
                "boolStorage.add(12345 != 12345);\n" +
                "boolStorage.add(12345 != 12346);\n" +
                "boolStorage.add(-12345 != 12345);\n" +
                "boolStorage.add(12345 != -12345);\n" +
                "boolStorage.add(12300 + 45 != 12000 + 345);\n" +
                "boolStorage.add(1 != 0);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(false, true, true, true, false, true));
    }

    @Test
    public void lessThanOperatorTest() {
        String code =
                "boolStorage.add(10000 < 10001);\n" +
                "boolStorage.add(10001 < 10000);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, false));
    }

    @Test
    public void greaterThanOperatorTest() {
        String code =
                "boolStorage.add(123456 > -1235456);\n" +
                "boolStorage.add(-1235456 > 123456);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, false));
    }

    @Test
    public void lessThanEqualsOperatorTest() {
        String code =
                "boolStorage.add(1000000 <= 1000000);\n" +
                "boolStorage.add(1000000 <= 1000001);\n" +
                "boolStorage.add(1000000 <= 999999);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, true, false));
    }

    @Test
    public void greaterThanEqualsOperatorTest() {
        String code =
                "boolStorage.add(1000000 >= 1000000);\n" +
                "boolStorage.add(1000001 >= 1000000);\n" +
                "boolStorage.add(999999 >= 1000000);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, true, false));
    }

    /*@Test
    public void floorDivTest() {
        String code =
                "boolStorage.add(123 !/ 10 == 12);\n" +
                "boolStorage.add(-1 !/ 10 == -1);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, true));
    }

    @Test
    public void floorModTest() {
        String code =
                "boolStorage.add(123 !% 10 == 3);\n" +
                "boolStorage.add(-1 !% 10 == 9);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, true));
    }*/

    @Test
    public void minusTest() {
        String code =
                "intStorage.add(-123);\n" +
                "intStorage.add(+123);\n" +
                "intStorage.add(- -123);\n" +
                "intStorage.add(- - -123);\n" +
                "intStorage.add(- - - + + + + + + + + +  + + +123);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(-123, 123, 123, -123, -123));
    }

    @Test
    public void incrementTest() {
        String code =
                "int x;\n" +
                "x++;\n" +
                "intStorage.add(x);\n" +
                "x++;\n" +
                "intStorage.add(x);\n" +
                "x++;\n" +
                "intStorage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(1, 2, 3));
    }

    @Test
    public void decrementTest() {
        String code =
                "int x;\n" +
                "x--;\n" +
                "intStorage.add(x);\n" +
                "x--;\n" +
                "intStorage.add(x);\n" +
                "x--;\n" +
                "intStorage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(-1, -2, -3));
    }

    @Test
    public void bitwiseTest() {
        String code =
                "intStorage.add(12345678 | 87654321);\n" +
                "intStorage.add(12345678 & 87654321);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(96305151, 3694848));
    }

    @Test
    public void augmentedAssignmentTest() {
        String code =
                "int x = 10;\n" +
                "x += 5;\n" +
                "intStorage.add(x);\n" +
                "\n" +
                "x -= 10;\n" +
                "intStorage.add(x);\n" +
                "\n" +
                "x *= 6;\n" +
                "intStorage.add(x);\n" +
                "\n" +
                "x /= 3;\n" +
                "intStorage.add(x);\n" +
                "\n" +
                "x %= 3;\n" +
                "intStorage.add(x);\n" +
                "\n" +
                "x &= 13;\n" +
                "intStorage.add(x);\n" +
                "\n" +
                "x |= 12;\n" +
                "intStorage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(15, 5, 30, 10, 1, 1, 13));
    }

    @Test
    public void toStringTest() {
        String code =
                "int x = 500;\n" +
                "stringStorage.add(x.toString());\n" +
                "stringStorage.add((400).toString());\n" +
                "\n" +
                "stringStorage.add((123456789).toStandardString());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                Lists.of("500", "400", "123,456,789"));
    }

    @Test
    public void tryParseTest() {
        String code =
                "int32 x;\n" +
                "boolStorage.add(int.tryParse(\"2010\", ref x));\n" +
                "intStorage.add(x);\n" +
                "\n" +
                "boolStorage.add(int32.tryParse(\"a\", ref x));\n" +
                "intStorage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, false));
        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(2010, 2010));
    }

    @Test
    public void literalWithDotTest() {
        String code =
                "let x = 10.toString();\n" +
                "stringStorage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("10"));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}