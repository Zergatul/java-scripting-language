package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class StaticPropertyTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void intStaticPropertiesTest() {
        String code = """
                intStorage.add(int.MIN_VALUE);
                intStorage.add(int.MAX_VALUE);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}