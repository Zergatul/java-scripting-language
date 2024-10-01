package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
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
                stringStorage.add("123" + '4');
                //
                stringStorage.add("-" + true);
                stringStorage.add("abc" + 123456);
                stringStorage.add("x" + 123.25);
                //
                stringStorage.add(false + "=");
                stringStorage.add(100 + "!");
                stringStorage.add(3.25 + "#");
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list,
                List.of(
                        "123789",
                        "1234",
                        "-true",
                        "abc123456",
                        "x123.25",
                        "false=",
                        "100!",
                        "3.25#"));
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

    @Test
    public void substringTest() {
        String code = """
                stringStorage.add("0123456789".substring(0));
                stringStorage.add("0123456789".substring(3));
                stringStorage.add("0123456789".substring(2, 4));
                stringStorage.add("0123456789".substring(4, 8));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list,
                List.of("0123456789", "3456789", "23", "4567"));
    }

    @Test
    public void containsTest() {
        String code = """
                boolStorage.add("banana".contains("ana"));
                boolStorage.add("banana".contains("anab"));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false));
    }

    @Test
    public void indexOfTest() {
        String code = """
                intStorage.add("banana".indexOf("ana"));
                intStorage.add("banana".indexOf("anab"));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, -1));
    }

    @Test
    public void startsWithTest() {
        String code = """
                boolStorage.add("banana".startsWith("bana"));
                boolStorage.add("banana".startsWith("anan"));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false));
    }

    @Test
    public void endsWithTest() {
        String code = """
                boolStorage.add("banana".endsWith("bana"));
                boolStorage.add("banana".endsWith("nana"));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(false, true));
    }

    @Test
    public void toLowerTest() {
        String code = """
                stringStorage.add("aBcDeF".toLower());
                stringStorage.add("Їжак".toLower());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("abcdef", "їжак"));
    }

    @Test
    public void toUpperTest() {
        String code = """
                stringStorage.add("aBcDeF".toUpper());
                stringStorage.add("Їжак".toUpper());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("ABCDEF", "ЇЖАК"));
    }

    @Test
    public void matches1Test() {
        String code = """
                boolStorage.add("banana".matches("an.na"));
                boolStorage.add("banana".matches("an..na"));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false));
    }

    @Test
    public void matches2Test() {
        String code = """
                boolStorage.add("BANANA".matches("an.na", 0x00));
                boolStorage.add("BANANA".matches("an.na", 0x02));
                boolStorage.add("BANANA".matches("an..na", 0x02));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(false, true, false));
    }

    @Test
    public void getMatches1Test() {
        String code = """
                string[] matches = "[1022] Log message.".getMatches("\\\\[(.+)\\\\]\\\\s+(.+)");
                intStorage.add(matches.length);
                foreach (string s in matches) stringStorage.add(s);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(3));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("[1022] Log message.", "1022", "Log message."));
    }

    @Test
    public void getMatches2Test() {
        String code = """
                string[] matches = "BANANA".getMatches("(an)", 0);
                intStorage.add(matches.length);
                foreach (string s in matches) stringStorage.add(s);
                
                matches = "BANANA".getMatches("(an)", 0x02);
                intStorage.add(matches.length);
                foreach (string s in matches) stringStorage.add(s);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(0, 2));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("AN", "AN"));
    }

    @Test
    public void replaceTest() {
        String code = """
                stringStorage.add("aaa".replace("aa", "b"));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("ba"));
    }

    public static class ApiRoot {
        public static StringStorage stringStorage;
        public static IntStorage intStorage;
        public static BoolStorage boolStorage;
    }
}