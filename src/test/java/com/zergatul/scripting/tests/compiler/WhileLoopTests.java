package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class WhileLoopTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void basicTest() {
        String code = """
                int i = 0;
                while (i < 10) {
                    intStorage.add(i);
                    i++;
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    @Test
    public void continueBreakTest() {
        String code = """
                int i = 0;
                int sum = 0;
                while (true) {
                    i++;
                    if (i < 10) continue;
                    if (i > 20) break;
                    sum += i;
                }
                intStorage.add(sum);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(165));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}