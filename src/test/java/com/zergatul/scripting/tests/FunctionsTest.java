package com.zergatul.scripting.tests;

import com.zergatul.scripting.old.compiler.ScriptCompileException;
import com.zergatul.scripting.old.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.helpers.FloatStorage;
import com.zergatul.scripting.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FunctionsTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
    }

    @Test
    public void voidFunctionTest() throws Exception {
        String code = """
                static int x;
                
                function simple() {
                    x++;
                }
                
                x = 123;
                for (int i = 0; i < 3; i++) {
                    simple();
                    intStorage.add(x);
                }
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(124, 125, 126));
    }

    @Test
    public void noReturnZeroStatementsTest() {
        String code = """
                function int func1() {}
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        assertThrows(ScriptCompileException.class, () -> compiler.compile(code));
    }

    @Test
    public void noReturnNonZeroStatementsTest() {
        String code = """
                function int func1() {
                    intStorage.add(123);
                }
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        assertThrows(ScriptCompileException.class, () -> compiler.compile(code));
    }

    @Test
    public void intFunctionTest() throws Exception {
        String code = """
                static int x = 123;
                static int y = 23;
                
                function int func1() {
                    if (x > y) {
                        x = x - y;
                        return x;
                    } else {
                        y = y - x;
                        return y;
                    }
                    return 0;
                }
                
                for (int i = 0; i < 8; i++) {
                    intStorage.add(func1());
                }
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(100, 77, 54, 31, 8, 15, 7, 1));
    }

    @Test
    public void booleanFunctionTest() throws Exception {
        String code = """
                function boolean func1() {
                    return true;
                }
                
                intStorage.add(func1() ? 3 : 2);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(3));
    }

    @Test
    public void floatFunctionTest() throws Exception {
        String code = """
                function float func1() {
                    return 123;
                }
                
                intStorage.add(func1() == 123 ? 3 : 2);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(3));
    }

    @Test
    public void stringFunctionTest() throws Exception {
        String code = """
                function string func1() {
                    return "abc";
                }
                
                intStorage.add(func1() == "abc" ? 3 : 2);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(3));
    }

    @Test
    public void arrayFunctionTest() throws Exception {
        String code = """
                function string[] func1() {
                    string[] result = new string[3];
                    result[0] = "a";
                    result[1] = "b";
                    result[2] = "c";
                    return result;
                }
                
                string[] array = func1();
                intStorage.add(array.length);
                intStorage.add(array[0] == "a" ? 5 : 0);
                intStorage.add(array[1] == "b" ? 4 : 0);
                intStorage.add(array[2] == "c" ? 3 : 0);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(3, 5, 4, 3));
    }

    @Test
    public void singleParamTest() throws Exception {
        String code = """
                function func(int x) {
                    intStorage.add(x + 1);
                }
                
                for (int i = 0; i < 5; i++) {
                    func(i);
                }
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1, 2, 3, 4, 5));
    }

    @Test
    public void doubleParamTest() throws Exception {
        String code = """
                function int sum(int x, int y) {
                    return x + y;
                }
                
                intStorage.add(sum(10, 10));
                intStorage.add(sum(25, 15));
                intStorage.add(sum(22, 12));
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(20, 40, 34));
    }

    @Test
    public void implicitCastParamTest() throws Exception {
        String code = """
                function float sum(float x, float y) {
                    return x + y;
                }
                
                int a = 100;
                floatStorage.add(sum(a, 10.5));
                floatStorage.add(sum(25.5, a));
                floatStorage.add(sum(22, 12));
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                List.of(110.5, 125.5, 34.0));
    }

    @Test
    public void recursiveTest() throws Exception {
        String code = """
                function int factorial(int x) {
                    if (x <= 1) {
                        return 1;
                    }
                    return x * factorial(x - 1);
                }
                
                intStorage.add(factorial(0));
                intStorage.add(factorial(1));
                intStorage.add(factorial(2));
                intStorage.add(factorial(5));
                intStorage.add(factorial(10));
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1, 1, 2, 120, 3628800));
    }

    @Test
    public void crossRecursionTest() throws Exception {
        String code = """
                function int strange(int x) {
                    if (x <= 1) {
                        return 1;
                    }
                    return 2 * func1(x - 1) + 3 * func2(x - 1);
                }
                
                function int func1(int x) {
                    return strange(x - 2) + 2;
                }
                
                function int func2(int x) {
                    return strange(x - 1) + 1;
                }
                
                intStorage.add(strange(1));
                intStorage.add(strange(2));
                intStorage.add(strange(3));
                intStorage.add(strange(4));
                intStorage.add(strange(5));
                intStorage.add(strange(6));
                intStorage.add(strange(7));
                intStorage.add(strange(8));
                intStorage.add(strange(20));
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1, 12, 12, 45, 67, 166, 298, 639, 2563221));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
    }
}