package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class CharTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void basicTest() {
        String code = """
                intStorage.add('a');
                intStorage.add('b');
                intStorage.add('c');
                intStorage.add('\\'');
                intStorage.add('\\n');
                intStorage.add('\\r');
                intStorage.add('\\t');
                intStorage.add('\\b');
                
                char ch = '!';
                intStorage.add(ch);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(97, 98, 99, 39, 10, 13, 9, 8, 33));
    }

    @Test
    public void lessThanTest() {
        String code = """
                boolStorage.add('a' < 'b');
                boolStorage.add('c' < 'c');
                boolStorage.add('f' < 'e');
                
                boolStorage.add('a' < 98);
                boolStorage.add(97  < 'b');
                boolStorage.add('c' < 99);
                boolStorage.add(99  < 'c');
                boolStorage.add('f' < 101);
                boolStorage.add(102 < 'e');
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, false, false, true, true, false, false, false, false));
    }

    @Test
    public void greaterThanTest() {
        String code = """
                boolStorage.add('a' > 'b');
                boolStorage.add('c' > 'c');
                boolStorage.add('f' > 'e');
                
                boolStorage.add('a' > 98);
                boolStorage.add(97  > 'b');
                boolStorage.add('c' > 99);
                boolStorage.add(99  > 'c');
                boolStorage.add('f' > 101);
                boolStorage.add(102 > 'e');
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(false, false, true, false, false, false, false, true, true));
    }

    @Test
    public void lessEqualsTest() {
        String code = """
                boolStorage.add('a' <= 'b');
                boolStorage.add('c' <= 'c');
                boolStorage.add('f' <= 'e');
                
                boolStorage.add('a' <= 98);
                boolStorage.add(97  <= 'b');
                boolStorage.add('c' <= 99);
                boolStorage.add(99  <= 'c');
                boolStorage.add('f' <= 101);
                boolStorage.add(102 <= 'e');
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true, false, true, true, true, true, false, false));
    }

    @Test
    public void greaterEqualsTest() {
        String code = """
                boolStorage.add('a' >= 'b');
                boolStorage.add('c' >= 'c');
                boolStorage.add('f' >= 'e');
                
                boolStorage.add('a' >= 98);
                boolStorage.add(97  >= 'b');
                boolStorage.add('c' >= 99);
                boolStorage.add(99  >= 'c');
                boolStorage.add('f' >= 101);
                boolStorage.add(102 >= 'e');
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(false, true, true, false, false, true, true, true, true));
    }

    @Test
    public void equalsTest() {
        String code = """
                boolStorage.add('a' == 'b');
                boolStorage.add('c' == 'c');
                boolStorage.add('f' == 'e');
                
                boolStorage.add('a' == 98);
                boolStorage.add(97  == 'b');
                boolStorage.add('c' == 99);
                boolStorage.add(99  == 'c');
                boolStorage.add('f' == 101);
                boolStorage.add(102 == 'e');
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(false, true, false, false, false, true, true, false, false));
    }

    @Test
    public void notEqualsTest() {
        String code = """
                boolStorage.add('a' != 'b');
                boolStorage.add('c' != 'c');
                boolStorage.add('f' != 'e');
                
                boolStorage.add('a' != 98);
                boolStorage.add(97  != 'b');
                boolStorage.add('c' != 99);
                boolStorage.add(99  != 'c');
                boolStorage.add('f' != 101);
                boolStorage.add(102 != 'e');
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, false, true, true, true, false, false, true, true));
    }

    @Test
    public void toStringTest() {
        String code = """
                stringStorage.add('a'.toString());
                stringStorage.add('1'.toString());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                List.of("a", "1"));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}