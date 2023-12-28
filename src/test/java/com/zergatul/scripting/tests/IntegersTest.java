package com.zergatul.scripting.tests;

import com.zergatul.scripting.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.helpers.BoolStorage;
import com.zergatul.scripting.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class IntegersTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void initialValueTest() throws Exception {
        String code = """
                int i;
                intStorage.add(i);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(0));
    }

    @Test
    public void initExpressionTest() throws Exception {
        String code = """
                int i = 123 + 456;
                intStorage.add(i);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(579));
    }

    @Test
    public void equalsOperatorTest() throws Exception {
        String code = """
                boolStorage.add(12345 == 12345);
                boolStorage.add(12345 == 12346);
                boolStorage.add(-12345 == 12345);
                boolStorage.add(12345 == -12345);
                boolStorage.add(12300 + 45 == 12000 + 345);
                boolStorage.add(1 == 0);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, false, false, false, true, false));
    }

    @Test
    public void notEqualsOperatorTest() throws Exception {
        String code = """
                boolStorage.add(12345 != 12345);
                boolStorage.add(12345 != 12346);
                boolStorage.add(-12345 != 12345);
                boolStorage.add(12345 != -12345);
                boolStorage.add(12300 + 45 != 12000 + 345);
                boolStorage.add(1 != 0);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(false, true, true, true, false, true));
    }

    @Test
    public void lessThanOperatorTest() throws Exception {
        String code = """
                boolStorage.add(10000 < 10001);
                boolStorage.add(10001 < 10000);
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
                boolStorage.add(123456 > -1235456);
                boolStorage.add(-1235456 > 123456);
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
                boolStorage.add(1000000 <= 1000000);
                boolStorage.add(1000000 <= 1000001);
                boolStorage.add(1000000 <= 999999);
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
                boolStorage.add(1000000 >= 1000000);
                boolStorage.add(1000001 >= 1000000);
                boolStorage.add(999999 >= 1000000);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true, false));
    }

    @Test
    public void floorDivTest() throws Exception {
        String code = """
                boolStorage.add(123 !/ 10 == 12);
                boolStorage.add(-1 !/ 10 == -1);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true));
    }

    @Test
    public void floorModTest() throws Exception {
        String code = """
                boolStorage.add(123 !% 10 == 3);
                boolStorage.add(-1 !% 10 == 9);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
    }
}