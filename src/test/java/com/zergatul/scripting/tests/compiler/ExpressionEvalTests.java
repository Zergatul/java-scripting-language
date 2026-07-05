package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.compiler.ExpressionCompilationResult;
import com.zergatul.scripting.parser.ParserErrors;
import com.zergatul.scripting.runtime.ExpressionEvaluationResult;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.tests.utility.MarkedCode;
import com.zergatul.scripting.type.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ExpressionEvalTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.test = new TestApi();
    }

    @Test
    public void nullLiteralTest() {
        Assertions.assertEquals(
                success(SJavaObject.instance, Object.class,"null"),
                evaluate("null"));
    }

    @Test
    public void boolLiteralTest() {
        Assertions.assertEquals(
                success(SBoolean.instance, boolean.class, "true"),
                evaluate("true"));
    }

    @Test
    public void intLiteralTest() {
        Assertions.assertEquals(
                success(SInt.instance, int.class, "10"),
                evaluate("10"));
    }

    @Test
    public void longLiteralTest() {
        Assertions.assertEquals(
                success(SInt64.instance, long.class, "10"),
                evaluate("10L"));
    }

    @Test
    public void stringLiteralTest() {
        Assertions.assertEquals(
                success(SString.instance, String.class, "Hello"),
                evaluate("\"Hello\""));
    }

    @Test
    public void voidTest() {
        Assertions.assertEquals(
                ExpressionEvaluationResult.fromVoid(),
                evaluate("intStorage.add(12)"));
        Assertions.assertIterableEquals(List.of(12), ApiRoot.intStorage.list);
    }

    @Test
    public void methodTest() {
        Assertions.assertEquals(
                success(SString.instance, String.class, "1.500"),
                evaluate("1.5.toStandardString(3)"));
    }

    @Test
    public void arrayTest() {
        noValueAssertEquals(
                success(new SArrayType(SInt.instance), int.class.arrayType(), ""),
                evaluate("[1, 2, 3]"));
    }

    @Test
    public void nullReturnValueTest() {
        Assertions.assertEquals(
                success(SString.instance, String.class,"null"),
                evaluate("test.getNullString()"));
    }

    @Test
    public void customToStringTest() {
        Assertions.assertEquals(
                success(SType.fromJavaType(CustomObject.class), CustomObject.class,"Hello from CustomObject"),
                evaluate("test.getCustomObject()"));
    }

    @Test
    public void conditionalExpressionTest1() {
        Assertions.assertEquals(
                success(SString.instance, String.class,"a"),
                evaluate("true ? \"a\" : null"));
    }

    @Test
    public void conditionalExpressionTest2() {
        Assertions.assertEquals(
                success(SString.instance, String.class,"null"),
                evaluate("false ? \"a\" : null"));
    }

    @Test
    public void mathExpressionTest() {
        Assertions.assertEquals(
                success(SInt.instance, int.class,"49"),
                evaluate("1 + 2 * 3 * (4 + 5) - 6"));
    }

    @Test
    public void exceptionTest1() {
        Assertions.assertEquals(
                ExpressionEvaluationResult.fromException(new ArithmeticException("/ by zero")),
                evaluate("1/0"));
    }

    @Test
    public void exceptionTest2() {
        Assertions.assertEquals(
                ExpressionEvaluationResult.fromException(new IllegalStateException("boom")),
                evaluate("test.exception()"));
    }

    @Test
    public void parserErrorTest1() {
        MarkedCode marked = MarkedCode.from("⟪let⟫ x = 3");
        assertEquals(
                fail(
                        new DiagnosticMessage(ParserErrors.ExpressionExpected, marked.getRange("⟪⟫"), "let"),
                        new DiagnosticMessage(ParserErrors.UnexpectedToken, marked.getRange("⟪⟫"), "let")),
                evaluate(marked.getCode()));
    }

    @Test
    public void parserErrorTest2() {
        MarkedCode marked = MarkedCode.from("1 + ⟪⟫");
        assertEquals(
                fail(new DiagnosticMessage(ParserErrors.ExpressionExpected, marked.getRange("⟪⟫"), "<EOF>")),
                evaluate(marked.getCode()));
    }

    @Test
    public void binderErrorTest1() {
        MarkedCode marked = MarkedCode.from("⟪intStorage + 1⟫");
        assertEquals(
                fail(new DiagnosticMessage(
                        BinderErrors.BinaryOperatorNotDefined,
                        marked.getRange("⟪⟫"),
                        "+", "Java<com.zergatul.scripting.tests.compiler.helpers.IntStorage>", "int")),
                evaluate(marked.getCode()));
    }

    private ExpressionEvaluationResult evaluate(String code) {
        CompilationParameters parameters = new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .build();
        Compiler compiler = new Compiler(parameters);
        ExpressionCompilationResult result = compiler.compileAsExpression(code);
        if (!result.isSuccessful()) {
            return ExpressionEvaluationResult.fromDiagnostics(result.getDiagnostics());
        }

        return result.getProgram().evaluate();
    }

    private static void assertEquals(ExpressionEvaluationResult expected, ExpressionEvaluationResult actual) {
        noValueAssertEquals(expected, actual);
        Assertions.assertEquals(expected.value(), actual.value());
    }

    private static void noValueAssertEquals(ExpressionEvaluationResult expected, ExpressionEvaluationResult actual) {
        Assertions.assertEquals(expected.ok(), actual.ok());
        Assertions.assertEquals(expected.hasValue(), actual.hasValue());
        Assertions.assertEquals(expected.type(), actual.type());
        Assertions.assertEquals(expected.javaType(), actual.javaType());
        comparator.assertEquals(expected.diagnostics(), actual.diagnostics());
    }

    private static ExpressionEvaluationResult success(SType type, Class<?> javaType, Object value) {
        return new ExpressionEvaluationResult(
                true, true,
                type.toString(), javaType.getCanonicalName(),
                value.toString(), List.of());
    }

    private static ExpressionEvaluationResult fail(DiagnosticMessage... messages) {
        return ExpressionEvaluationResult.fromDiagnostics(List.of(messages));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static TestApi test;
    }

    @SuppressWarnings("unused")
    public static class TestApi {
        public String getNullString() {
            return null;
        }
        public Object getCustomObject() {
            return new CustomObject();
        }
        public void exception() {
            throw new IllegalStateException("boom");
        }
    }

    public static class CustomObject {
        @Override
        public String toString() {
            return "Hello from CustomObject";
        }
    }
}