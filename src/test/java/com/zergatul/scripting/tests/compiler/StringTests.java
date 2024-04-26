package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.helpers.IntStorage;
import com.zergatul.scripting.helpers.StringStorage;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class StringTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.boolStorage = new BoolStorage();
    }

    @Test
    public void initialValueTest() {
        String code = """
                string s;
                stringStorage.add(s);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                List.of(""));
    }

    @Test
    public void initExpressionTest() {
        String code = """
                string s = "test" + "123";
                stringStorage.add(s);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                List.of("test123"));
    }

    @Test
    public void concatTest() {
        String code = """
                stringStorage.add("123" + "789");
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("123789"));
    }

    @Test
    public void lengthTest() {
        String code = """
                string s1 = "123456789";
                intStorage.add(s1.length);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(9));
    }

    @Test
    public void equalsOperatorTest() {
        String code = """
                boolStorage.add("abcd" == "abcd");
                boolStorage.add("ab" + "cd" == "a" + "bcd");
                boolStorage.add("abcd" == "abcde");
                boolStorage.add("abcde" == "abcde" + "q");
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, true, false, false));
    }

    @Test
    public void notEqualsOperatorTest() {
        String code = """
                boolStorage.add("abcd" != "abcd");
                boolStorage.add("ab" + "cd" != "a" + "bcd");
                boolStorage.add("abcd" != "abcde");
                boolStorage.add("abcde" != "abcde" + "q");
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(false, false, true, true));
    }

    @Test
    public void characterIndexerTest() {
        String code = """
                string s = "abc";
                intStorage.add(s[0]);
                intStorage.add(s[1]);
                intStorage.add(s[2]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(97, 98, 99));
    }

    public static class ApiRoot {
        public static StringStorage stringStorage;
        public static IntStorage intStorage;
        public static BoolStorage boolStorage;
    }
}