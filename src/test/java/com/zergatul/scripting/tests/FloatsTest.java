package com.zergatul.scripting.tests;

import com.zergatul.scripting.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.helpers.BoolStorage;
import com.zergatul.scripting.helpers.FloatStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class FloatsTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.floatStorage = new FloatStorage();
    }

    @Test
    public void initialValueTest() throws Exception {
        String code = """
                float f;
                floatStorage.add(f);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                List.of(0.0));
    }

    @Test
    public void initExpressionTest() throws Exception {
        String code = """
                float f = 1.5 + 5.5;
                floatStorage.add(f);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                List.of(7.0));
    }

    @Test
    public void equalsOperatorTest() throws Exception {
        String code = """
                boolStorage.add(0.5 == 0.5);
                boolStorage.add(1.5 + 1.5 == 0.5 + 2.5);
                boolStorage.add(0.5 == 0.6);
                boolStorage.add(1.5 + 1.6 == 0.5 + 2.5);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true, false, false));
    }

    @Test
    public void notEqualsOperatorTest() throws Exception {
        String code = """
                boolStorage.add(0.5 != 0.5);
                boolStorage.add(1.5 + 1.5 != 0.5 + 2.5);
                boolStorage.add(0.5 != 0.6);
                boolStorage.add(1.5 + 1.6 != 0.5 + 2.5);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(false, false, true, true));
    }

    @Test
    public void lessThanOperatorTest() throws Exception {
        String code = """
                boolStorage.add(10000.0 < 10001.0);
                boolStorage.add(10001.0 < 10000.0);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, false));
    }

    @Test
    public void greaterThanOperatorTest() throws Exception {
        String code = """
                boolStorage.add(123456.0 > -1235456.0);
                boolStorage.add(-1235456.0 > 123456.0);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, false));
    }

    @Test
    public void lessThanEqualsOperatorTest() throws Exception {
        String code = """
                boolStorage.add(1000000.0 <= 1000000.0);
                boolStorage.add(1000000.0 <= 1000001.0);
                boolStorage.add(1000000.0 <= 999999.0);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true, false));
    }

    @Test
    public void greaterThanEqualsOperatorTest() throws Exception {
        String code = """
                boolStorage.add(1000000.0 >= 1000000.0);
                boolStorage.add(1000001.0 >= 1000000.0);
                boolStorage.add(999999.0 >= 1000000.0);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true, false));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static FloatStorage floatStorage;
    }
}