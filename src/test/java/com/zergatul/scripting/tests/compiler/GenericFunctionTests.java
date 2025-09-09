package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class GenericFunctionTests extends ComparatorTest {

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

    @Test
    public void returnInnerLambdaTest1() {
        String code = """
                fn<int => fn<int => int>> func(int a) =>
                    x => y => (x + y) * a;
                
                intStorage.add(func(9)(8)(7));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of((8 + 7) * 9));
    }

    @Test
    public void returnInnerLambdaTest2() {
        String code = """
                fn<int => fn<int => fn<int => int>>> func(int a) =>
                    x => y => z => (x * y + z) * a;
                
                intStorage.add(func(9)(8)(7)(6));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of((8 * 7 + 6) * 9));
    }

    @Test
    public void variableDeclarationTest1() {
        String code = """
                fn<(int, int, int) => int> func = (a, b, c) => a * b + c;
                intStorage.add(func(7, 8, 9));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(65));
    }

    @Test
    public void variableDeclarationTest2() {
        String code = """
                let func = (a, b, c) => a * b + c;
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(BinderErrors.LetUnboundLambda, new SingleLineTextRange(1, 1, 0, 3))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void classMethodAsFunctionSimpleTest() {
        String code = """
                class MyClass {
                    int delta;
                    constructor(int delta) { this.delta = delta; }
                    void add(int x) { intStorage.add(this.delta + x); }
                }
                
                fn<int => void> func = new MyClass(10).add;
                func(1); func(3); func(5);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(11, 13, 15));
    }

    @Test
    public void classMethodAsFunctionOverloadTest() {
        String code = """
                class MyClass {
                    int delta;
                    constructor(int delta) { this.delta = delta; }
                    void add(int x) { intStorage.add(this.delta + x); }
                    void add(int x, int y) { intStorage.add(this.delta + x + y); }
                    void add(int x, int y, int z) { intStorage.add(this.delta + x + y * z); }
                }
                
                fn<int => void> func1 = new MyClass(10).add;
                fn<(int, int) => void> func2 = new MyClass(10).add;
                fn<(int, int, int) => void> func3 = new MyClass(10).add;
                
                func1(8);
                func2(9, 9);
                func3(15, 3, 8);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(18, 28, 49));
    }

    @Test
    public void classMethodAsFunctionNoOverloadTest() {
        String code = """
                class MyClass {
                    int delta;
                    constructor(int delta) { this.delta = delta; }
                    void add(int x, int y) { intStorage.add(this.delta + x + y); }
                }
                
                fn<int => void> func1 = new MyClass(10).add;
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(BinderErrors.CannotImplicitlyConvert, new SingleLineTextRange(7, 25, 176, 19),
                        "<MethodGroup>", "fn<int => void>")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void classFieldTest() {
        String code = """
                class MyClass {
                    fn<int => int> func;
                }
                
                let c = new MyClass();
                c.func = x => x * x;
                intStorage.add(c.func(3));
                intStorage.add(c.func(5));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(9, 25));
    }

    @Test
    public void localFunctionCaptureTest() {
        String code = """
                int x = 3;
                fn<int => int> add = a => a + x;
                intStorage.add(add(5));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(8));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}