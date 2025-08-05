package com.zergatul.scripting.tests.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class FieldTests {

    @Test
    public void setFieldTest() {
        String code = """
                let instance = api.getTestClass();
                instance.field = "qwe";
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertEquals(ApiRoot.api.getTestClass().field, "qwe");
    }

    public static class ApiRoot {
        public static Api api = new Api();
    }

    public static class Api {

        private final TestClass instance = new TestClass();

        public TestClass getTestClass() {
            return instance;
        }
    }

    public static class TestClass {
        public String field = "123";
    }
}