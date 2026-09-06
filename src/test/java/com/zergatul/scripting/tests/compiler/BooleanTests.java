package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.*;

public class BooleanTests {

    @BeforeEach
    public void clean() {
        ApiRoot.storage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void initialValueTest() {
        String code =
                "boolean b;\n" +
                "storage.add(b);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                Lists.of(false));
    }

    @Test
    public void initExpressionTest() {
        String code =
                "boolean b = true || false;\n" +
                "storage.add(b);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                Lists.of(true));
    }

    @Test
    public void constantsTest() {
        String code =
                "storage.add(true);\n" +
                "storage.add(false);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                Lists.of(true, false));
    }

    @Test
    public void notOperatorTest() {
        String code =
                "storage.add(!true);\n" +
                "storage.add(!false);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                Lists.of(false, true));
    }

    @Test
    public void equalsOperatorTest() {
        String code =
                "storage.add(true == true);\n" +
                "storage.add(false == false);\n" +
                "storage.add(true == false);\n" +
                "storage.add(false == true);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                Lists.of(true, true, false, false));
    }

    @Test
    public void notEqualsOperatorTest() {
        String code =
                "storage.add(true != true);\n" +
                "storage.add(false != false);\n" +
                "storage.add(true != false);\n" +
                "storage.add(false != true);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                Lists.of(false, false, true, true));
    }

    @Test
    public void andOperatorTest() {
        String code =
                "storage.add(true && true);\n" +
                "storage.add(false && false);\n" +
                "storage.add(true && false);\n" +
                "storage.add(false && true);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                Lists.of(true, false, false, false));
    }

    @Test
    public void orOperatorTest() {
        String code =
                "storage.add(true || true);\n" +
                "storage.add(false || false);\n" +
                "storage.add(true || false);\n" +
                "storage.add(false || true);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                Lists.of(true, false, true, true));
    }

    @Test
    public void lessThanOperatorTest() {
        String code =
                "storage.add(true < true);\n" +
                "storage.add(false < false);\n" +
                "storage.add(true < false);\n" +
                "storage.add(false < true);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                Lists.of(false, false, false, true));
    }

    @Test
    public void greaterThanOperatorTest() {
        String code =
                "storage.add(true > true);\n" +
                "storage.add(false > false);\n" +
                "storage.add(true > false);\n" +
                "storage.add(false > true);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                Lists.of(false, false, true, false));
    }

    @Test
    public void lessEqualsOperatorTest() {
        String code =
                "storage.add(true <= true);\n" +
                "storage.add(false <= false);\n" +
                "storage.add(true <= false);\n" +
                "storage.add(false <= true);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                Lists.of(true, true, false, true));
    }

    @Test
    public void greaterEqualsOperatorTest() {
        String code =
                "storage.add(true >= true);\n" +
                "storage.add(false >= false);\n" +
                "storage.add(true >= false);\n" +
                "storage.add(false >= true);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                Lists.of(true, true, true, false));
    }

    @Test
    public void bitwiseVsBooleanOr() {
        String code =
                "boolean getFalse() {\n" +
                "    intStorage.add(101);\n" +
                "    return false;\n" +
                "}\n" +
                "boolean getTrue() {\n" +
                "    intStorage.add(102);\n" +
                "    return true;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(getTrue() | getFalse() ? 201 : 202);\n" +
                "intStorage.add(getFalse() | getTrue() ? 203 : 204);\n" +
                "\n" +
                "intStorage.add(getTrue() || getFalse() ? 205 : 206);\n" +
                "intStorage.add(getFalse() || getTrue() ? 207 : 208);\n" +
                "\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(
                        102, 101, 201,
                        101, 102, 203,
                        102, 205,
                        101, 102, 207));
    }

    @Test
    public void bitwiseVsBooleanAnd() {
        String code =
                "boolean getFalse() {\n" +
                "    intStorage.add(101);\n" +
                "    return false;\n" +
                "}\n" +
                "boolean getTrue() {\n" +
                "    intStorage.add(102);\n" +
                "    return true;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(getTrue() & getFalse() ? 201 : 202);\n" +
                "intStorage.add(getFalse() & getTrue() ? 203 : 204);\n" +
                "\n" +
                "intStorage.add(getTrue() && getFalse() ? 205 : 206);\n" +
                "intStorage.add(getFalse() && getTrue() ? 207 : 208);\n" +
                "\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(
                        102, 101, 202,
                        101, 102, 204,
                        102, 101, 206,
                        101, 208));
    }

    @Test
    public void augmentedAssignmentTest() {
        String code =
                "boolean b = false;\n" +
                "\n" +
                "b |= false;\n" +
                "storage.add(b);\n" +
                "\n" +
                "b |= true;\n" +
                "storage.add(b);\n" +
                "\n" +
                "b &= true;\n" +
                "storage.add(b);\n" +
                "\n" +
                "b &= false;\n" +
                "storage.add(b);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                Lists.of(false, true, true, false));
    }

    @Test
    public void complexExpressionTest() {
        String code =
                "storage.add(1 < 2 && 3 < 4);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                Lists.of(true));
    }

    @Test
    public void toStringTest() {
        String code =
                "stringStorage.add(false.toString());\n" +
                "stringStorage.add(true.toString());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                Lists.of("false", "true"));
    }

    public static class ApiRoot {
        public static BoolStorage storage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}