package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class GenericFunctionTests {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void conversionFromFunctionTest() {
        String code = """
                int f1() => 10;
                int f2() => 20;
                int f3() => 30;
                fn<() => int> func(int p) {
                    if (p == 1) return f1;
                    if (p == 2) return f2;
                    return f3;
                }
                
                let g1 = func(1);
                let g2 = func(2);
                let g3 = func(3);
                intStorage.add(g1());
                intStorage.add(g2());
                intStorage.add(g3());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 20, 30));
    }

    @Test
    public void argumentTest() {
        String code = """
                int f1() => 10;
                int f2() => 20;
                int f3() => 30;
                int sqr(fn<() => int> func) {
                    return func() * func();
                }
                
                intStorage.add(sqr(f1));
                intStorage.add(sqr(f2));
                intStorage.add(sqr(f3));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(100, 400, 900));
    }

    @Test
    public void voidTest() {
        String code = """
                void f1() => intStorage.add(1);
                void f2() => intStorage.add(2);
                void f3() => intStorage.add(3);
                void run(fn<() => void> func) => func();
                
                run(f3);
                run(f2);
                run(f1);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(3, 2, 1));
    }

    @Test
    public void returnLambdaTest() {
        String code = """
                int sum(int x1, int x2) => x1 + x2;
                fn<int => int> add(int x) => y => sum(x, y);
                
                let add1 = add(1);
                let add5 = add(5);
                let add8 = add(8);
                intStorage.add(add1(10));
                intStorage.add(add5(20));
                intStorage.add(add8(30));
                intStorage.add(add1(40));
                intStorage.add(add5(50));
                intStorage.add(add8(60));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11, 25, 38, 41, 55, 68));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}
