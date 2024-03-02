package com.zergatul.scripting.tests;

import com.zergatul.scripting.old.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ForLoopsTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void breakStatementTest() throws Exception {
        String code = """
                for (;;) {
                    intStorage.add(10);
                    break;
                    intStorage.add(20);
                }
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(10));
    }

    @Test
    public void continueStatementTest() throws Exception {
        String code = """
                int sum;
                for (int i = 10; i >= 0; i--) {
                    if (i > 5) {
                        continue;
                    }
                    sum = sum + i;
                }
                intStorage.add(sum);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(15));
    }

    @Test
    public void loopArrayAssignTest() throws Exception {
        String code = """
                int[] a = new int[10];
                for (int i = 0; i < a.length; i++) {
                    a[i] = i + 1;
                }
                for (int i = 0; i < a.length; i++) {
                    intStorage.add(a[i]);
                }
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void noInitializerTest() throws Exception {
        String code = """
                int sum = 0;
                int j = 0;
                for (; j <= 10;) {
                    sum = sum + j;
                    j = j + 1;
                }
                intStorage.add(sum);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(55));
    }

    @Test
    public void backwardsTest() throws Exception {
        String code = """
                int result = 1;
                for (int i = 10; i >= 0; i--) {
                    int mode = i % 3;
                    if (mode == 0) {
                        result = result + i;
                    }
                    if (mode == 1) {
                        result = result - i;
                    }
                    if (mode == 2) {
                        result = result * i;
                    }
                }
                intStorage.add(result);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(-13));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}