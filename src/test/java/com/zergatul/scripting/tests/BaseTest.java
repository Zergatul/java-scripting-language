package com.zergatul.scripting.tests;

import com.zergatul.scripting.old.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.helpers.BoolStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BaseTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
    }

    @Test
    public void endCommentTest() throws Exception {
        String code = """
                // aaa
                // bbb
                // ccc
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();
    }

    @Test
    public void nestedMethodTest() throws Exception {
        String code = """
                boolStorage.add(deep.getValue() == 987);
                boolStorage.add(deep.deep.getValue() == 101);
                boolStorage.add(deep.deep.deep.getValue() == 654);
                boolStorage.add(deep.deep.deep.deep.getValue() == 321);
                boolStorage.add(deep.deep.deep.deep.deep.getValue() == 100);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true, true, true, true));
    }

    public static class ApiRoot {
        public static Deep0 deep = new Deep0();
        public static BoolStorage boolStorage;
    }

    public static class Deep0 {

        public Deep1 deep = new Deep1();

        public int getValue() {
            return 987;
        }
    }

    public static class Deep1 {

        public Deep2 deep = new Deep2();

        public int getValue() {
            return 101;
        }
    }

    public static class Deep2 {

        public Deep3 deep = new Deep3();

        public int getValue() {
            return 654;
        }
    }

    public static class Deep3 {

        public Deep4 deep = new Deep4();

        public int getValue() {
            return 321;
        }
    }

    public static class Deep4 {

        public int getValue() {
            return 100;
        }
    }
}