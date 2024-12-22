package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.type.CustomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compileWithCustomType;

public class StaticMethodTests {

    @BeforeEach
    public void clean() {
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void simpleTest() {
        String code = """
                stringStorage.add(SomeClass.get());
                stringStorage.add(SomeClass.get());
                stringStorage.add(SomeClass.get());
                """;

        Runnable program = compileWithCustomType(ApiRoot.class, SomeClass.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("x1", "x2", "x3"));
    }

    public static class ApiRoot {
        public static StringStorage stringStorage;
    }

    @CustomType(name = "SomeClass")
    public static class SomeClass {

        private static int number = 1;

        public static String get() {
            return "x" + (number++);
        }
    }
}