package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

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
        String code =
                "int f1() => 10;\n" +
                "int f2() => 20;\n" +
                "int f3() => 30;\n" +
                "fn<() => int> func(int p) {\n" +
                "    if (p == 1) return f1;\n" +
                "    if (p == 2) return f2;\n" +
                "    return f3;\n" +
                "}\n" +
                "\n" +
                "let g1 = func(1);\n" +
                "let g2 = func(2);\n" +
                "let g3 = func(3);\n" +
                "intStorage.add(g1());\n" +
                "intStorage.add(g2());\n" +
                "intStorage.add(g3());\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 20, 30));
    }

    @Test
    public void argumentTest() {
        String code =
                "int f1() => 10;\n" +
                "int f2() => 20;\n" +
                "int f3() => 30;\n" +
                "int sqr(fn<() => int> func) {\n" +
                "    return func() * func();\n" +
                "}\n" +
                "\n" +
                "intStorage.add(sqr(f1));\n" +
                "intStorage.add(sqr(f2));\n" +
                "intStorage.add(sqr(f3));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(100, 400, 900));
    }

    @Test
    public void voidTest() {
        String code =
                "void f1() => intStorage.add(1);\n" +
                "void f2() => intStorage.add(2);\n" +
                "void f3() => intStorage.add(3);\n" +
                "void run(fn<() => void> func) => func();\n" +
                "\n" +
                "run(f3);\n" +
                "run(f2);\n" +
                "run(f1);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(3, 2, 1));
    }

    @Test
    public void returnLambdaTest() {
        String code =
                "int sum(int x1, int x2) => x1 + x2;\n" +
                "fn<int => int> add(int x) => y => sum(x, y);\n" +
                "\n" +
                "let add1 = add(1);\n" +
                "let add5 = add(5);\n" +
                "let add8 = add(8);\n" +
                "intStorage.add(add1(10));\n" +
                "intStorage.add(add5(20));\n" +
                "intStorage.add(add8(30));\n" +
                "intStorage.add(add1(40));\n" +
                "intStorage.add(add5(50));\n" +
                "intStorage.add(add8(60));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(11, 25, 38, 41, 55, 68));
    }

    @Test
    public void returnInnerLambdaTest1() {
        String code =
                "fn<int => fn<int => int>> func(int a) =>\n" +
                "    x => y => (x + y) * a;\n" +
                "\n" +
                "intStorage.add(func(9)(8)(7));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of((8 + 7) * 9));
    }

    @Test
    public void returnInnerLambdaTest2() {
        String code =
                "fn<int => fn<int => fn<int => int>>> func(int a) =>\n" +
                "    x => y => z => (x * y + z) * a;\n" +
                "\n" +
                "intStorage.add(func(9)(8)(7)(6));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of((8 * 7 + 6) * 9));
    }

    @Test
    public void variableDeclarationTest1() {
        String code =
                "fn<(int, int, int) => int> func = (a, b, c) => a * b + c;\n" +
                "intStorage.add(func(7, 8, 9));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(65));
    }

    @Test
    public void variableDeclarationTest2() {
        String code =
                "let func = (a, b, c) => a * b + c;\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.LetUnboundLambda, new SingleLineTextRange(1, 1, 0, 3))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void classMethodAsFunctionSimpleTest() {
        String code =
                "class MyClass {\n" +
                "    int delta;\n" +
                "    constructor(int delta) { this.delta = delta; }\n" +
                "    void add(int x) { intStorage.add(this.delta + x); }\n" +
                "}\n" +
                "\n" +
                "fn<int => void> func = new MyClass(10).add;\n" +
                "func(1); func(3); func(5);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(11, 13, 15));
    }

    @Test
    public void classMethodAsFunctionOverloadTest() {
        String code =
                "class MyClass {\n" +
                "    int delta;\n" +
                "    constructor(int delta) { this.delta = delta; }\n" +
                "    void add(int x) { intStorage.add(this.delta + x); }\n" +
                "    void add(int x, int y) { intStorage.add(this.delta + x + y); }\n" +
                "    void add(int x, int y, int z) { intStorage.add(this.delta + x + y * z); }\n" +
                "}\n" +
                "\n" +
                "fn<int => void> func1 = new MyClass(10).add;\n" +
                "fn<(int, int) => void> func2 = new MyClass(10).add;\n" +
                "fn<(int, int, int) => void> func3 = new MyClass(10).add;\n" +
                "\n" +
                "func1(8);\n" +
                "func2(9, 9);\n" +
                "func3(15, 3, 8);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(18, 28, 49));
    }

    @Test
    public void classMethodAsFunctionNoOverloadTest() {
        String code =
                "class MyClass {\n" +
                "    int delta;\n" +
                "    constructor(int delta) { this.delta = delta; }\n" +
                "    void add(int x, int y) { intStorage.add(this.delta + x + y); }\n" +
                "}\n" +
                "\n" +
                "fn<int => void> func1 = new MyClass(10).add;\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.CannotImplicitlyConvert, new SingleLineTextRange(7, 25, 176, 19),
                        "<MethodGroup>", "fn<int => void>")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void classFieldTest() {
        String code =
                "class MyClass {\n" +
                "    fn<int => int> func;\n" +
                "}\n" +
                "\n" +
                "let c = new MyClass();\n" +
                "c.func = x => x * x;\n" +
                "intStorage.add(c.func(3));\n" +
                "intStorage.add(c.func(5));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(9, 25));
    }

    @Test
    public void localFunctionCaptureTest() {
        String code =
                "int x = 3;\n" +
                "fn<int => int> add = a => a + x;\n" +
                "intStorage.add(add(5));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(8));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}