package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.Run;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class FunctionTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.run = new Run();
    }

    @Test
    public void voidFunctionTest() {
        String code = """
                static int x;
                
                void simple() {
                    x++;
                }
                
                x = 123;
                for (int i = 0; i < 3; i++) {
                    simple();
                    intStorage.add(x);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(124, 125, 126));
    }

    @Test
    public void noReturnZeroStatementsTest() {
        String code = """
                int func1() {}
                intStorage.add(func1());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(0));
    }

    @Test
    public void intFunctionTest() {
        String code = """
                static int x = 123;
                static int y = 23;
                
                int func1() {
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

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(100, 77, 54, 31, 8, 15, 7, 1));
    }

    @Test
    public void booleanFunctionTest() {
        String code = """
                boolean func1() {
                    return true;
                }
                
                intStorage.add(func1() ? 3 : 2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(3));
    }

    @Test
    public void floatFunctionTest() {
        String code = """
                float func1() {
                    return 123;
                }
                
                intStorage.add(func1() == 123 ? 3 : 2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(3));
    }

    @Test
    public void stringFunctionTest() {
        String code = """
                string func1() {
                    return "abc";
                }
                
                intStorage.add(func1() == "abc" ? 3 : 2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(3));
    }

    @Test
    public void arrayFunctionTest() {
        String code = """
                string[] func1() {
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

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(3, 5, 4, 3));
    }

    @Test
    public void singleParamTest() {
        String code = """
                void func(int x) {
                    intStorage.add(x + 1);
                }
                
                for (int i = 0; i < 5; i++) {
                    func(i);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1, 2, 3, 4, 5));
    }

    @Test
    public void doubleParamTest() {
        String code = """
                int sum(int x, int y) {
                    return x + y;
                }
                
                intStorage.add(sum(10, 10));
                intStorage.add(sum(25, 15));
                intStorage.add(sum(22, 12));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(20, 40, 34));
    }

    @Test
    public void implicitCastParamTest() {
        String code = """
                float sum(float x, float y) {
                    return x + y;
                }
                
                int a = 100;
                floatStorage.add(sum(a, 10.5));
                floatStorage.add(sum(25.5, a));
                floatStorage.add(sum(22, 12));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                List.of(110.5, 125.5, 34.0));
    }

    @Test
    public void recursiveTest() {
        String code = """
                int factorial(int x) {
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

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1, 1, 2, 120, 3628800));
    }

    @Test
    public void crossRecursionTest() {
        String code = """
                int strange(int x) {
                    if (x <= 1) {
                        return 1;
                    }
                    return 2 * func1(x - 1) + 3 * func2(x - 1);
                }
                
                int func1(int x) {
                    return strange(x - 2) + 2;
                }
                
                int func2(int x) {
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

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1, 12, 12, 45, 67, 166, 298, 639, 2563221));
    }

    @Test
    public void asLambdaTest1() {
        String code = """
                void func() {
                    intStorage.add(25);
                }
                
                run.once(func);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(25));
    }

    @Test
    public void asLambdaTest2() {
        String code = """
                void func(string s) {
                    stringStorage.add(s + "!");
                }
                
                run.onString(func);
                run.triggerString("a");
                run.triggerString("b");
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("a!", "b!"));
    }

    @Test
    public void asLambdaTest3() {
        String code = """
                void func(int i, string s) {
                    intStorage.add(i + 1);
                    stringStorage.add(s + "!");
                }
                
                run.onIntString(func);
                run.triggerIntString(10, "a");
                run.triggerIntString(20, "b");
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11, 21));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("a!", "b!"));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
        public static Run run;
    }
}