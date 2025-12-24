package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.ObjectStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class ClassBinaryOperationTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.objectStorage = new ObjectStorage();
    }

    @Test
    public void basicTest() {
        String code = """
                class Vec2 {
                    float x;
                    float y;
                
                    constructor(float x, float y) {
                        this.x = x;
                        this.y = y;
                    }
                
                    override string toString() {
                        return "(" + x + "; " + y + ")";
                    }
                
                    operator [+] Vec2(Vec2 left, Vec2 right) {
                        return new Vec2(left.x + right.x, left.y + right.y);
                    }
                
                    operator [-] Vec2(Vec2 left, Vec2 right) {
                        return new Vec2(left.x - right.x, left.y - right.y);
                    }
                
                    operator [*] Vec2(float factor, Vec2 vec) {
                        return new Vec2(factor * vec.x, factor * vec.y);
                    }
                
                    operator [/] Vec2(Vec2 vec, float divisor) {
                        return new Vec2(vec.x / divisor, vec.y / divisor);
                    }
                
                    operator [%] Vec2(Vec2 vec, float divisor) {
                        return new Vec2(vec.x % divisor, vec.y % divisor);
                    }
                
                    operator [==] boolean(Vec2 left, Vec2 right) => left.x == right.x && left.y == right.y;
                    operator [!=] boolean(Vec2 left, Vec2 right) => left.x != right.x || left.y != right.y;
                }
                
                void log(Vec2 v) => stringStorage.add(v.toString());
                
                let v1 = new Vec2(1, 2);
                let v2 = new Vec2(3, 4);
                log(v1 + v2);
                log(v1 - v2);
                log(2 * v1);
                log(v2 / 2);
                log(v1 % 2);
                boolStorage.add(v1 == v2);
                boolStorage.add(v1 != v2);
                boolStorage.add(v1 == new Vec2(1, 2));
                boolStorage.add(v1 != new Vec2(1, 2));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                List.of(
                        "(4.0; 6.0)",
                        "(-2.0; -2.0)",
                        "(2.0; 4.0)",
                        "(1.5; 2.0)",
                        "(1.0; 0.0)"));
        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(
                        false, true,
                        true, false));
    }

    @Test
    public void canBeOverloadedTest() {
        String code = """
                class MyClass {
                    operator [&&] boolean(MyClass left, MyClass right) => true;
                    operator [||] boolean(MyClass left, MyClass right) => true;
                    operator [is] boolean(MyClass left, MyClass right) => true;
                    operator [as] boolean(MyClass left, MyClass right) => true;
                    operator [in] boolean(MyClass left, MyClass right) => true;
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.BinaryOperatorCannotBeOverloaded, new SingleLineTextRange(2, 15, 30, 2), BinaryOperator.BOOLEAN_AND),
                        new DiagnosticMessage(BinderErrors.BinaryOperatorCannotBeOverloaded, new SingleLineTextRange(3, 15, 94, 2), BinaryOperator.BOOLEAN_OR),
                        new DiagnosticMessage(BinderErrors.BinaryOperatorCannotBeOverloaded, new SingleLineTextRange(4, 15, 158, 2), BinaryOperator.IS),
                        new DiagnosticMessage(BinderErrors.BinaryOperatorCannotBeOverloaded, new SingleLineTextRange(5, 15, 222, 2), BinaryOperator.AS),
                        new DiagnosticMessage(BinderErrors.BinaryOperatorCannotBeOverloaded, new SingleLineTextRange(6, 15, 286, 2), BinaryOperator.IN)),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void doubleOverloadTest() {
        String code = """
                class MyClass {
                    operator [*] boolean(MyClass left, int right) => true;
                    operator [*] boolean(MyClass left, int right) => false;
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.BinaryOperationAlreadyDeclared, new SingleLineTextRange(3, 25, 99, 25))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void booleanOnlyOverloadTest() {
        String code = """
                class MyClass {
                    operator [==] MyClass(MyClass left, int right) => null;
                    operator [!=] MyClass(MyClass left, int right) => null;
                    operator [>] MyClass(MyClass left, int right) => null;
                    operator [<] MyClass(MyClass left, int right) => null;
                    operator [>=] MyClass(MyClass left, int right) => null;
                    operator [<=] MyClass(MyClass left, int right) => null;
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.BinaryOperatorCanReturnBooleanOnly, new SingleLineTextRange(2, 19, 34, 7), BinaryOperator.EQUALS),
                        new DiagnosticMessage(BinderErrors.BinaryOperatorCanReturnBooleanOnly, new SingleLineTextRange(3, 19, 94, 7), BinaryOperator.NOT_EQUALS),
                        new DiagnosticMessage(BinderErrors.BinaryOperatorCanReturnBooleanOnly, new SingleLineTextRange(4, 18, 153, 7), BinaryOperator.GREATER),
                        new DiagnosticMessage(BinderErrors.BinaryOperatorCanReturnBooleanOnly, new SingleLineTextRange(5, 18, 212, 7), BinaryOperator.LESS),
                        new DiagnosticMessage(BinderErrors.BinaryOperatorCanReturnBooleanOnly, new SingleLineTextRange(6, 19, 272, 7), BinaryOperator.GREATER_EQUALS),
                        new DiagnosticMessage(BinderErrors.BinaryOperatorCanReturnBooleanOnly, new SingleLineTextRange(7, 19, 332, 7), BinaryOperator.LESS_EQUALS)),
                getDiagnostics(ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static ObjectStorage objectStorage;
    }
}