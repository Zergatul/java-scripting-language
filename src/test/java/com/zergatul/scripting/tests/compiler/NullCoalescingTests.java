package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class NullCoalescingTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void basicTest() {
        String code =
                "string getStr1() => null;\n" +
                "string getStr2() => \"y\";\n" +
                "\n" +
                "stringStorage.add(getStr1() ?? \"x\");\n" +
                "stringStorage.add(getStr2() ?? \"x\");\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("x", "y"), ApiRoot.stringStorage.list);
    }

    @Test
    public void throwTest() {
        String code =
                "typealias RuntimeException = Java<java.lang.RuntimeException>;\n" +
                "\n" +
                "string getStr() => null;\n" +
                "\n" +
                "stringStorage.add(getStr() ?? throw new RuntimeException());\n";

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(RuntimeException.class, program::run);
    }

    @Test
    public void rightAssociativityTest1() {
        String code =
                "class Animal {}\n" +
                "class Dog : Animal {}\n" +
                "class Cat : Animal {}\n" +
                "\n" +
                "Dog dog = null;\n" +
                "Cat cat = null;\n" +
                "Animal animal = new Animal();\n" +
                "boolStorage.add((dog ?? cat ?? animal) is not null);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(true), ApiRoot.boolStorage.list);
    }

    @Test
    public void rightAssociativityTest2() {
        String code =
                "class Animal {}\n" +
                "class Dog : Animal {}\n" +
                "class Cat : Animal {}\n" +
                "\n" +
                "Dog dog = null;\n" +
                "Cat cat = new Cat();\n" +
                "Animal animal = null;\n" +
                "boolStorage.add((animal ?? dog ?? cat) is not null);\n";

        comparator.assertEquals(
                Lists.of(
                        new DiagnosticMessage(
                                BinderErrors.BinaryOperatorNotDefined,
                                new SingleLineTextRange(8, 28, 147, 10),
                                "??", "Dog", "Cat")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void augmentedAssignmentTest1() {
        String code =
                "string str = null;\n" +
                "str ??= \"none\";\n" +
                "stringStorage.add(str);\n" +
                "str ??= \"x\";\n" +
                "stringStorage.add(str);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of("none", "none"), ApiRoot.stringStorage.list);
    }

    @Test
    public void augmentedAssignmentTest2() {
        String code =
                "string str = null;\n" +
                "str ??= throw new Java<java.lang.RuntimeException>();\n";

        Runnable program = compile(ApiRoot.class, code);
        Assertions.assertThrows(RuntimeException.class, program::run);
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
    }
}