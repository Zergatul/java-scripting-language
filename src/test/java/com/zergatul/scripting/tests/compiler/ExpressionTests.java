package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class ExpressionTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void popExpressionReturnFromStackTest() {
        String code = """
                int get() {
                    return 123;
                }
                
                int count = 0;
                for (int i = 0; i < 1000000000; i++) {
                    get();
                    count++;
                }
                
                intStorage.add(count);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1000000000));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}