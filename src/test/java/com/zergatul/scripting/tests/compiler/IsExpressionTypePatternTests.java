package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.type.CustomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compileWithCustomType;

public class IsExpressionTypePatternTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.api = new Api();
    }

    @Test
    public void boolTest() {
        String code =
                "boolStorage.add(false is boolean);\n" +
                "boolStorage.add(1 is boolean);\n" +
                "boolStorage.add(\"qwe\" is boolean);\n" +
                "boolStorage.add(false is not boolean);\n" +
                "boolStorage.add(1 is not boolean);\n" +
                "boolStorage.add(\"qwe\" is not boolean);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(
                        true, false, false,
                        false, true, true));
    }

    @Test
    public void intTest() {
        String code =
                "boolStorage.add(0 is int);\n" +
                "boolStorage.add(1.1 is int);\n" +
                "boolStorage.add(\"qwe\" is int);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false, false));
    }

    @Test
    public void charTest() {
        String code =
                "boolStorage.add('a' is char);\n" +
                "boolStorage.add(1 is char);\n" +
                "boolStorage.add(\"qwe\" is char);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false, false));
    }

    @Test
    public void stringTest() {
        String code =
                "boolStorage.add(\"1\" is string);\n" +
                "boolStorage.add(1 is string);\n" +
                "boolStorage.add(false is string);\n" +
                "boolStorage.add(\"1\" is not string);\n" +
                "boolStorage.add(1 is not string);\n" +
                "boolStorage.add(false is not string);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                Lists.of(
                        true, false, false,
                        false, true, true));
    }

    @Test
    public void arrayTest() {
        String code =
                "boolStorage.add([1, 2, 3] is int[]);\n" +
                "boolStorage.add([\"1\"] is int[]);\n" +
                "boolStorage.add([false] is int[]);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false, false));
    }

    @Test
    public void customTypeTest() {
        String code =
                "let instance = api.getSomething();\n" +
                "boolStorage.add(instance is MyType);\n" +
                "boolStorage.add(instance is int);\n" +
                "boolStorage.add(instance is string);\n" +
                "boolStorage.add([instance] is MyType[]);\n" +
                "boolStorage.add([[instance]] is MyType[][]);\n" +
                "boolStorage.add([instance] is MyType[][]);\n";

        Runnable program = compileWithCustomType(ApiRoot.class, MyType.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true, false, false, true, true, false));
    }

    @Test
    public void nullTest() {
        String code =
                "let instance = api.getNull();\n" +
                "boolStorage.add(instance is MyType);\n" +
                "instance = api.getSomething();\n" +
                "boolStorage.add(instance is MyType);\n";

        Runnable program = compileWithCustomType(ApiRoot.class, MyType.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(false, true));
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