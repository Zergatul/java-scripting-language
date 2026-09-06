package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class NestedClassTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
    }

    @Test
    public void nestedMethodTest() {
        String code =
                "boolStorage.add(deep.getValue() == 987);\n" +
                "boolStorage.add(deep.deep.getValue() == 101);\n" +
                "boolStorage.add(deep.deep.deep.getValue() == 654);\n" +
                "boolStorage.add(deep.deep.deep.deep.getValue() == 321);\n" +
                "boolStorage.add(deep.deep.deep.deep.deep.getValue() == 100);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(true, true, true, true, true));
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