package com.zergatul.scripting.tests;

import com.zergatul.scripting.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.helpers.BoolStorage;
import com.zergatul.scripting.helpers.IntStorage;
import com.zergatul.scripting.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class StringsTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.boolStorage = new BoolStorage();
    }

    @Test
    public void initialValueTest() throws Exception {
        String code = """
                string s;
                stringStorage.add(s);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                List.of(""));
    }

    @Test
    public void initExpressionTest() throws Exception {
        String code = """
                string s = "qqq" + "w";
                stringStorage.add(s);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.stringStorage.list,
                List.of("qqqw"));
    }

    @Test
    public void concatTest() throws Exception {
        String code = """
                stringStorage.add("123" + "789");
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("123789"));
    }

    @Test
    public void lengthTest() throws Exception {
        String code = """
                string s1 = "123456789";
                intStorage.add(s1.length);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(9));
    }

    @Test
    public void equalsOperatorTest() throws Exception {
        String code = """
                boolStorage.add("abcd" == "abcd");
                boolStorage.add("ab" + "cd" == "a" + "bcd");
                boolStorage.add("abcd" == "abcde");
                boolStorage.add("abcde" == "abcde" + "q");
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, true, false, false));
    }

    @Test
    public void notEqualsOperatorTest() throws Exception {
        String code = """
                boolStorage.add("abcd" != "abcd");
                boolStorage.add("ab" + "cd" != "a" + "bcd");
                boolStorage.add("abcd" != "abcde");
                boolStorage.add("abcde" != "abcde" + "q");
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(false, false, true, true));
    }

    public static class ApiRoot {
        public static StringStorage stringStorage;
        public static IntStorage intStorage;
        public static BoolStorage boolStorage;
    }
}