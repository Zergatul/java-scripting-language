package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class JavaTypeTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.api = new Api();
    }

    @Test
    public void basicTest() {
        String code = """
                Java<java.lang.Object> o = api.getObject();
                intStorage.add(o.hashCode());
                stringStorage.add(o.toString());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(ApiRoot.api.getObject().hashCode()));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of(ApiRoot.api.getObject().toString()));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static Api api;
    }

    public static class Api {

        private final Object object = new Object();

        public Object getObject() {
            return object;
        }
    }
}