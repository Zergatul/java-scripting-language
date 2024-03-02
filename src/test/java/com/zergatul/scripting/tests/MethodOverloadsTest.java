package com.zergatul.scripting.tests;

import com.zergatul.scripting.old.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.helpers.FloatStorage;
import com.zergatul.scripting.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MethodOverloadsTest {

    @BeforeEach
    public void clean() {
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void simpleTest() throws Exception {
        String code = """
                stringStorage.add(methods.toString(0));
                stringStorage.add(methods.toString(0.0));
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                List.of("int", "float"));
    }

    @Test
    public void upcastTest() throws Exception {
        String code = """
                floatStorage.add(methods.m1(1, 2, 3, ""));
                floatStorage.add(methods.m1(1, 2.0, 3, ""));
                floatStorage.add(methods.m1(1, 2, 3, 4));
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                List.of(1.0, 2.0, 1.0));
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