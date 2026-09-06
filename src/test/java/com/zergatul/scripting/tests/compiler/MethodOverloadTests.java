package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class MethodOverloadTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void simpleTest() {
        String code =
                "stringStorage.add(methods.toString(0));\n" +
                "stringStorage.add(methods.toString(0.0));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                Lists.of("int", "float"));
    }

    @Test
    public void upcastTest() {
        String code =
                "floatStorage.add(methods.m1(1, 2, 3, \"\"));\n" +
                "floatStorage.add(methods.m1(1, 2.0, 3, \"\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                Lists.of(1.0, 2.0));
    }

    @Test
    public void noOverloadTest() {
        String code =
                "floatStorage.add(methods.m1⟦(1, 2, 3, 4)⟧);\n";

        String candidates =
                "Candidates:\n" +
                "float m1(float x, float y, float z, string s)\n" +
                "float m1(int x, int y, int z, string s)";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.MethodInvalidArguments,
                "m1", candidates);
    }

    @SuppressWarnings("unused")
    public static class ApiRoot {
        public static Methods methods = new Methods();
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
    }

    @SuppressWarnings("unused")
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