package com.zergatul.scripting.tests.compiler;

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
    public void lessThanOperatorTest() {
        String code = """
                storage.add(true < true);
                storage.add(false < false);
                storage.add(true < false);
                storage.add(false < true);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(false, false, false, true));
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

    @Test
    public void lessEqualsOperatorTest() {
        String code = """
                storage.add(true <= true);
                storage.add(false <= false);
                storage.add(true <= false);
                storage.add(false <= true);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(true, true, false, true));
    }

    @Test
    public void greaterEqualsOperatorTest() {
        String code = """
                storage.add(true >= true);
                storage.add(false >= false);
                storage.add(true >= false);
                storage.add(false >= true);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(true, true, true, false));
    }

    @Test
    public void bitwiseVsBooleanOr() {
        String code = """
                boolean getFalse() {
                    intStorage.add(101);
                    return false;
                }
                boolean getTrue() {
                    intStorage.add(102);
                    return true;
                }
                
                intStorage.add(getTrue() | getFalse() ? 201 : 202);
                intStorage.add(getFalse() | getTrue() ? 203 : 204);
                
                intStorage.add(getTrue() || getFalse() ? 205 : 206);
                intStorage.add(getFalse() || getTrue() ? 207 : 208);
                
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(
                        102, 101, 201,
                        101, 102, 203,
                        102, 205,
                        101, 102, 207));
    }

    @Test
    public void bitwiseVsBooleanAnd() {
        String code = """
                boolean getFalse() {
                    intStorage.add(101);
                    return false;
                }
                boolean getTrue() {
                    intStorage.add(102);
                    return true;
                }
                
                intStorage.add(getTrue() & getFalse() ? 201 : 202);
                intStorage.add(getFalse() & getTrue() ? 203 : 204);
                
                intStorage.add(getTrue() && getFalse() ? 205 : 206);
                intStorage.add(getFalse() && getTrue() ? 207 : 208);
                
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(
                        102, 101, 202,
                        101, 102, 204,
                        102, 101, 206,
                        101, 208));
    }

    @Test
    public void augmentedAssignmentTest() {
        String code = """
                boolean b = false;

                b |= false;
                storage.add(b);
                
                b |= true;
                storage.add(b);
                
                b &= true;
                storage.add(b);
                
                b &= false;
                storage.add(b);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(false, true, true, false));
    }

    @Test
    public void complexExpressionTest() {
        String code = """
                storage.add(1 < 2 && 3 < 4);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(true));
    }

    @Test
    public void toStringTest() {
        String code = """
                stringStorage.add(false.toString());
                stringStorage.add(true.toString());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                List.of("false", "true"));
    }

    public static class ApiRoot {
        public static BoolStorage storage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}