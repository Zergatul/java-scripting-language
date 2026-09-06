package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Collections;

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
        String code =
                "string s;\n" +
                "stringStorage.add(s);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                Lists.of(""));
    }

    @Test
    public void initExpressionTest() {
        String code =
                "string s = \"test\" + \"123\";\n" +
                "stringStorage.add(s);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                Lists.of("test123"));
    }

    @Test
    public void concatTest() {
        String code =
                "stringStorage.add(\"123\" + \"789\");\n" +
                "stringStorage.add(\"123\" + '4');\n" +
                "//\n" +
                "stringStorage.add(\"-\" + true);\n" +
                "stringStorage.add(\"abc\" + 123456);\n" +
                "stringStorage.add(\"x\" + 123.25);\n" +
                "//\n" +
                "stringStorage.add(false + \"=\");\n" +
                "stringStorage.add(100 + \"!\");\n" +
                "stringStorage.add(3.25 + \"#\");\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list,
                Lists.of(
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
    public void concatChainTest() {
        String code =
                "class Value {\n" +
                "    int value;\n" +
                "\n" +
                "    constructor(int value) {\n" +
                "        this.value = value;\n" +
                "    }\n" +
                "\n" +
                "    override string toString() {\n" +
                "        intStorage.add(value);\n" +
                "        return value.toString();\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "stringStorage.add(\"[\" + new Value(1) + '-' + new Value(2) + 3 + ']');\n" +
                "stringStorage.add(\"sum=\" + (1 + 2) + \".\");\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("[1-23]", "sum=3."));
        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 2));
    }

    @Test
    public void longConcatChainTest() {
        String expression = String.join(" + ", Collections.nCopies(250, "\"x\""));
        Runnable program = compile(ApiRoot.class, "stringStorage.add(" + expression + ");");
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of(String.join("", Collections.nCopies(250, "x"))));
    }

    @Test
    public void lengthTest() {
        String code =
                "string s1 = \"123456789\";\n" +
                "intStorage.add(s1.length);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(9));
    }

    @Test
    public void equalsOperatorTest() {
        String code =
                "boolStorage.add(\"abcd\" == \"abcd\");\n" +
                "boolStorage.add(\"ab\" + \"cd\" == \"a\" + \"bcd\");\n" +
                "boolStorage.add(\"abcd\" == \"abcde\");\n" +
                "boolStorage.add(\"abcde\" == \"abcde\" + \"q\");\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, true, false, false));
    }

    @Test
    public void notEqualsOperatorTest() {
        String code =
                "boolStorage.add(\"abcd\" != \"abcd\");\n" +
                "boolStorage.add(\"ab\" + \"cd\" != \"a\" + \"bcd\");\n" +
                "boolStorage.add(\"abcd\" != \"abcde\");\n" +
                "boolStorage.add(\"abcde\" != \"abcde\" + \"q\");\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(false, false, true, true));
    }

    @Test
    public void characterIndexerTest() {
        String code =
                "string s = \"abc\";\n" +
                "intStorage.add(s[0]);\n" +
                "intStorage.add(s[1]);\n" +
                "intStorage.add(s[2]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(97, 98, 99));
    }

    @Test
    public void substringTest() {
        String code =
                "stringStorage.add(\"0123456789\".substring(0));\n" +
                "stringStorage.add(\"0123456789\".substring(3));\n" +
                "stringStorage.add(\"0123456789\".substring(2, 4));\n" +
                "stringStorage.add(\"0123456789\".substring(4, 8));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list,
                Lists.of("0123456789", "3456789", "23", "4567"));
    }

    @Test
    public void containsTest() {
        String code =
                "boolStorage.add(\"banana\".contains(\"ana\"));\n" +
                "boolStorage.add(\"banana\".contains(\"anab\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false));
    }

    @Test
    public void indexOfTest() {
        String code =
                "intStorage.add(\"banana\".indexOf(\"ana\"));\n" +
                "intStorage.add(\"banana\".indexOf(\"anab\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, -1));
    }

    @Test
    public void startsWithTest() {
        String code =
                "boolStorage.add(\"banana\".startsWith(\"bana\"));\n" +
                "boolStorage.add(\"banana\".startsWith(\"anan\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false));
    }

    @Test
    public void endsWithTest() {
        String code =
                "boolStorage.add(\"banana\".endsWith(\"bana\"));\n" +
                "boolStorage.add(\"banana\".endsWith(\"nana\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(false, true));
    }

    @Test
    public void toLowerTest() {
        String code =
                "stringStorage.add(\"aBcDeF\".toLower());\n" +
                "stringStorage.add(\"Їжак\".toLower());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("abcdef", "їжак"));
    }

    @Test
    public void toUpperTest() {
        String code =
                "stringStorage.add(\"aBcDeF\".toUpper());\n" +
                "stringStorage.add(\"Їжак\".toUpper());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("ABCDEF", "ЇЖАК"));
    }

    @Test
    public void matches1Test() {
        String code =
                "boolStorage.add(\"banana\".matches(\"an.na\"));\n" +
                "boolStorage.add(\"banana\".matches(\"an..na\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false));
    }

    @Test
    public void matches2Test() {
        String code =
                "boolStorage.add(\"BANANA\".matches(\"an.na\", 0x00));\n" +
                "boolStorage.add(\"BANANA\".matches(\"an.na\", 0x02));\n" +
                "boolStorage.add(\"BANANA\".matches(\"an..na\", 0x02));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(false, true, false));
    }

    @Test
    public void getMatches1Test() {
        String code =
                "string[] matches = \"[1022] Log message.\".getMatches(\"\\\\[(.+)\\\\]\\\\s+(.+)\");\n" +
                "intStorage.add(matches.length);\n" +
                "foreach (string s in matches) stringStorage.add(s);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(3));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("[1022] Log message.", "1022", "Log message."));
    }

    @Test
    public void getMatches2Test() {
        String code =
                "string[] matches = \"BANANA\".getMatches(\"(an)\", 0);\n" +
                "intStorage.add(matches.length);\n" +
                "foreach (string s in matches) stringStorage.add(s);\n" +
                "\n" +
                "matches = \"BANANA\".getMatches(\"(an)\", 0x02);\n" +
                "intStorage.add(matches.length);\n" +
                "foreach (string s in matches) stringStorage.add(s);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(0, 2));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("AN", "AN"));
    }

    @Test
    public void replaceTest() {
        String code =
                "stringStorage.add(\"aaa\".replace(\"aa\", \"b\"));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("ba"));
    }

    @Test
    public void splitTest() {
        String code =
                "foreach (let part in \"aa bbb cccc\".split(\" \")) {\n" +
                "    stringStorage.add(part);\n" +
                "}\n" +
                "foreach (let part in \"qwe abc \".split(' ')) {\n" +
                "    stringStorage.add(part);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("aa", "bbb", "cccc", "qwe", "abc", ""));
    }

    @Test
    public void regexSplitTest() {
        String code =
                "foreach (let part in \"aa  bb   cc\".regexSplit(\"\\\\s+\")) {\n" +
                "    stringStorage.add(part);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("aa", "bb", "cc"));
    }

    public static class ApiRoot {
        public static StringStorage stringStorage;
        public static IntStorage intStorage;
        public static BoolStorage boolStorage;
    }
}