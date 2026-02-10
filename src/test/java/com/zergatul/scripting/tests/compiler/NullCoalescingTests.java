package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class NullCoalescingTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void basicTest() {
        String code = """
                string getStr1() => null;
                string getStr2() => "y";
                
                stringStorage.add(getStr1() ?? "x");
                stringStorage.add(getStr2() ?? "x");
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of("x", "y"), ApiRoot.stringStorage.list);
    }

    @Test
    public void throwTest() {
        String code = """
                typealias RuntimeException = Java<java.lang.RuntimeException>;
                
                string getStr() => null;
                
                stringStorage.add(getStr() ?? throw new RuntimeException());
                """;

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(RuntimeException.class, program::run);
    }

    @Test
    public void rightAssociativityTest1() {
        String code = """
                class Animal {}
                class Dog : Animal {}
                class Cat : Animal {}
                
                Dog dog = null;
                Cat cat = null;
                Animal animal = new Animal();
                boolStorage.add((dog ?? cat ?? animal) is not null);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(true), ApiRoot.boolStorage.list);
    }

    @Test
    public void rightAssociativityTest2() {
        String code = """
                class Animal {}
                class Dog : Animal {}
                class Cat : Animal {}
                
                Dog dog = null;
                Cat cat = new Cat();
                Animal animal = null;
                boolStorage.add((animal ?? dog ?? cat) is not null);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(List.of(true), ApiRoot.boolStorage.list);
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
    }
}