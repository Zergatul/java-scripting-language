package com.zergatul.scripting.tests;

import com.zergatul.scripting.old.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ForEachLoopsTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void breakStatementTest() throws Exception {
        String code = """
                int[] array = new int[10];
                for (int i = 0; i < array.length; i++) {
                    array[i] = (i + 1) * 10;
                }
                foreach (int x in array) {
                    if (x > 50) {
                        break;
                    }
                    intStorage.add(x);
                }
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(10, 20, 30, 40, 50));
    }

    @Test
    public void continueStatementTest() throws Exception {
        String code = """
                int[] array = new int[10];
                for (int i = 0; i < array.length; i++) {
                    array[i] = i + 1;
                }
                foreach (int x in array) {
                    if (x % 2 == 0) {
                        continue;
                    }
                    intStorage.add(x);
                }
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1, 3, 5, 7, 9));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}