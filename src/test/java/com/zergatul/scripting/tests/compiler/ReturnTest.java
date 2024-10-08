package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class ReturnTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void simpleTest() {
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

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(15));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}