package com.zergatul.scripting.tests;

import com.zergatul.scripting.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ReturnsTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void simpleTest() throws Exception {
        String code = """
                int a1 = 123;
                int a2 = 456;
                if (a1 > a2) {
                    return;
                }
                intStorage.add(15);
                if (a1 < a2) {
                    return;
                }
                intStorage.add(16);
                return;
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(15));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}