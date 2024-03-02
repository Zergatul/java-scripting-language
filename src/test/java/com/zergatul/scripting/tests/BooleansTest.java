package com.zergatul.scripting.tests;

import com.zergatul.scripting.old.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.helpers.BoolStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BooleansTest {

    @BeforeEach
    public void clean() {
        ApiRoot.storage = new BoolStorage();
    }

    @Test
    public void initialValueTest() throws Exception {
        String code = """
                boolean b;
                storage.add(b);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(false));
    }

    @Test
    public void initExpressionTest() throws Exception {
        String code = """
                boolean b = true || false;
                storage.add(b);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(true));
    }

    @Test
    public void constantsTest() throws Exception {
        String code = """
                storage.add(true);
                storage.add(false);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(true, false));
    }

    @Test
    public void notOperatorTest() throws Exception {
        String code = """
                storage.add(!true);
                storage.add(!false);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(false, true));
    }

    @Test
    public void equalsOperatorTest() throws Exception {
        String code = """
                storage.add(true == true);
                storage.add(false == false);
                storage.add(true == false);
                storage.add(false == true);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(true, true, false, false));
    }

    @Test
    public void notEqualsOperatorTest() throws Exception {
        String code = """
                storage.add(true != true);
                storage.add(false != false);
                storage.add(true != false);
                storage.add(false != true);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(false, false, true, true));
    }

    @Test
    public void andOperatorTest() throws Exception {
        String code = """
                storage.add(true && true);
                storage.add(false && false);
                storage.add(true && false);
                storage.add(false && true);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(true, false, false, false));
    }

    @Test
    public void orOperatorTest() throws Exception {
        String code = """
                storage.add(true || true);
                storage.add(false || false);
                storage.add(true || false);
                storage.add(false || true);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(true, false, true, true));
    }

    @Test
    public void greaterThanOperatorTest() throws Exception {
        String code = """
                storage.add(true > true);
                storage.add(false > false);
                storage.add(true > false);
                storage.add(false > true);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(false, false, true, false));
    }

    public static class ApiRoot {
        public static BoolStorage storage;
    }
}