package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.helpers.BoolStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.*;

public class BooleanTests {

    @BeforeEach
    public void clean() {
        BooleanTests.ApiRoot.storage = new BoolStorage();
    }

    @Test
    public void initialValueTest() {
        String code = """
                boolean b;
                storage.add(b);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(false));
    }

    @Test
    public void initExpressionTest() {
        String code = """
                boolean b = true || false;
                storage.add(b);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(true));
    }

    @Test
    public void constantsTest() {
        String code = """
                storage.add(true);
                storage.add(false);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(true, false));
    }

    @Test
    public void notOperatorTest() {
        String code = """
                storage.add(!true);
                storage.add(!false);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(false, true));
    }

    @Test
    public void equalsOperatorTest() {
        String code = """
                storage.add(true == true);
                storage.add(false == false);
                storage.add(true == false);
                storage.add(false == true);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(true, true, false, false));
    }

    @Test
    public void notEqualsOperatorTest() {
        String code = """
                storage.add(true != true);
                storage.add(false != false);
                storage.add(true != false);
                storage.add(false != true);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(false, false, true, true));
    }

    @Test
    public void andOperatorTest() {
        String code = """
                storage.add(true && true);
                storage.add(false && false);
                storage.add(true && false);
                storage.add(false && true);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(true, false, false, false));
    }

    @Test
    public void orOperatorTest() {
        String code = """
                storage.add(true || true);
                storage.add(false || false);
                storage.add(true || false);
                storage.add(false || true);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(true, false, true, true));
    }

    @Test
    public void greaterThanOperatorTest() {
        String code = """
                storage.add(true > true);
                storage.add(false > false);
                storage.add(true > false);
                storage.add(false > true);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(false, false, true, false));
    }

    public static class ApiRoot {
        public static BoolStorage storage;
    }
}