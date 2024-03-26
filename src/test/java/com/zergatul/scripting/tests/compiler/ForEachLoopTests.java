package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class ForEachLoopTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void breakStatementTest() {
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

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(10, 20, 30, 40, 50));
    }

    @Test
    public void continueStatementTest()  {
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

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1, 3, 5, 7, 9));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}