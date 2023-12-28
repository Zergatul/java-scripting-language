package com.zergatul.scripting.tests;

import com.zergatul.scripting.helpers.IntStorage;
import com.zergatul.scripting.compiler.ScriptingLanguageCompiler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ArraysTest {

    @BeforeEach
    public void clean() {
        ApiRoot.storage = new IntStorage();
    }

    @Test
    public void initialValueTest() throws Exception {
        String code = """
                int[] array;
                storage.add(array.length);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(0));
    }

    @Test
    public void simpleTest() throws Exception {
        String code = """
                int[] data3 = new int[5];
                data3[0] = 10;
                data3[1] = 20;
                data3[2] = 30;
                data3[3] = 40;
                data3[4] = 50;
                storage.add(data3[0]);
                storage.add(data3[1]);
                storage.add(data3[2]);
                storage.add(data3[3]);
                storage.add(data3[4]);
                storage.add(data3.length);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(10, 20, 30, 40, 50, 5));
    }

    @Test
    public void incrementOperatorTest() throws Exception {
        String code = """
                int[] array = new int[5];
                storage.add(array[0]);
                array[0]++;
                storage.add(array[0]);
                array[0]++;
                storage.add(array[0]);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.storage.list,
                List.of(0, 1, 2));
    }

    public static class ApiRoot {
        public static IntStorage storage;
    }
}