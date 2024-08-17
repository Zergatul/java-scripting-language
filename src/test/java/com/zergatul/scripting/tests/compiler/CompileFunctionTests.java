package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CompileFunctionTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void boolFunction1Test() {
        String code = """
                return intStorage.last() >= 100;
                """;

        BoolFunction program = compile(code, BoolFunction.class);

        ApiRoot.intStorage.add(100);
        Assertions.assertTrue(program.isOk());

        ApiRoot.intStorage.add(200);
        Assertions.assertTrue(program.isOk());

        ApiRoot.intStorage.add(99);
        Assertions.assertFalse(program.isOk());

        ApiRoot.intStorage.add(0);
        Assertions.assertFalse(program.isOk());
    }

    @Test
    public void boolFunction2Test() {
        String code = """
                return value1 < value2;
                """;

        BoolFunction2 program = compile(code, BoolFunction2.class);

        Assertions.assertTrue(program.check(1, 2));
        Assertions.assertTrue(program.check(10, 20));
        Assertions.assertFalse(program.check(2, 1));
        Assertions.assertFalse(program.check(10, 10));
    }

    private static <T> T compile(String code, Class<T> clazz) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .setInterface(clazz)
                //.setDebug()
                .build());
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.getDiagnostics());
        return result.getProgram();
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }

    @FunctionalInterface
    public interface BoolFunction {
        boolean isOk();
    }

    @FunctionalInterface
    public interface BoolFunction2 {
        boolean check(int value1, int value2);
    }
}