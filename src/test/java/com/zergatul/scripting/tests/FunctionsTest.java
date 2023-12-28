package com.zergatul.scripting.tests;

import com.zergatul.scripting.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class FunctionsTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void simpleTest() throws Exception {
        String code = """
                static int x;
                
                function simple() {
                    x++;
                }
                
                x = 123;
                for (int i = 0; i < 3; i++) {
                    simple();
                    intStorage.add(x);
                }
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(124, 125, 126));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}