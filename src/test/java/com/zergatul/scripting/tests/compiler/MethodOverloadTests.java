package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class MethodOverloadTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void simpleTest() {
        String code = """
                stringStorage.add(methods.toString(0));
                stringStorage.add(methods.toString(0.0));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                List.of("int", "float"));
    }

    @Test
    public void upcastTest() {
        String code = """
                floatStorage.add(methods.m1(1, 2, 3, ""));
                floatStorage.add(methods.m1(1, 2.0, 3, ""));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                List.of(1.0, 2.0));
    }

    @Test
    public void noOverloadTest() {
        String code = """
                floatStorage.add(methods.m1(1, 2, 3, 4));
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(
                        BinderErrors.CannotCastArguments,
                        new SingleLineTextRange(1, 28,27, 12))),
                getDiagnostics(ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static Methods methods = new Methods();
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
    }

    public static class Methods {

        public double m1(int x, int y, int z, String s) {
            return x;
        }

        public double m1(double x, double y, double z, String s) {
            return y;
        }

        public String toString(int value) {
            return "int";
        }

        public String toString(double value) {
            return "float";
        }
    }
}