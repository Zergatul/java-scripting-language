package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.type.CustomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compileWithCustomType;

public class TypeTestExpressionTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.api = new Api();
    }

    @Test
    public void boolTest() {
        String code = """
                boolStorage.add(false is boolean);
                boolStorage.add(1 is boolean);
                boolStorage.add("qwe" is boolean);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false, false));
    }

    @Test
    public void intTest() {
        String code = """
                boolStorage.add(0 is int);
                boolStorage.add(1.1 is int);
                boolStorage.add("qwe" is int);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false, false));
    }

    @Test
    public void charTest() {
        String code = """
                boolStorage.add('a' is char);
                boolStorage.add(1 is char);
                boolStorage.add("qwe" is char);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false, false));
    }

    @Test
    public void stringTest() {
        String code = """
                boolStorage.add("1" is string);
                boolStorage.add(1 is string);
                boolStorage.add(false is string);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false, false));
    }

    @Test
    public void arrayTest() {
        String code = """
                boolStorage.add([1, 2, 3] is int[]);
                boolStorage.add(["1"] is int[]);
                boolStorage.add([false] is int[]);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false, false));
    }

    @Test
    public void customTypeTest() {
        String code = """
                let instance = api.getSomething();
                boolStorage.add(instance is MyType);
                boolStorage.add(instance is int);
                boolStorage.add(instance is string);
                boolStorage.add([instance] is MyType[]);
                boolStorage.add([[instance]] is MyType[][]);
                boolStorage.add([instance] is MyType[][]);
                """;

        Runnable program = compileWithCustomType(ApiRoot.class, MyType.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, false, false, true, true, false));
    }

    @Test
    public void nullTest() {
        String code = """
                let instance = api.getNull();
                boolStorage.add(instance is MyType);
                instance = api.getSomething();
                boolStorage.add(instance is MyType);
                """;

        Runnable program = compileWithCustomType(ApiRoot.class, MyType.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(false, true));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static Api api;
    }

    @SuppressWarnings("unused")
    public static class Api {
        public MyType getSomething() {
            return new MyType();
        }
        public MyType getNull() {
            return null;
        }
    }

    @CustomType(name = "MyType")
    public static class MyType {}
}