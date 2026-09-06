package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.parser.ParserErrors;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.Run;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class FunctionTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.run = new Run();
    }

    @Test
    public void voidFunctionTest() {
        String code =
                "static int x;\n" +
                "\n" +
                "void simple() {\n" +
                "    x++;\n" +
                "}\n" +
                "\n" +
                "x = 123;\n" +
                "for (int i = 0; i < 3; i++) {\n" +
                "    simple();\n" +
                "    intStorage.add(x);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(124, 125, 126));
    }

    @Test
    public void intFunctionTest() {
        String code =
                "static int x = 123;\n" +
                "static int y = 23;\n" +
                "\n" +
                "int func1() {\n" +
                "    if (x > y) {\n" +
                "        x = x - y;\n" +
                "        return x;\n" +
                "    } else {\n" +
                "        y = y - x;\n" +
                "        return y;\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n" +
                "\n" +
                "for (int i = 0; i < 8; i++) {\n" +
                "    intStorage.add(func1());\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(100, 77, 54, 31, 8, 15, 7, 1));
    }

    @Test
    public void booleanFunctionTest() {
        String code =
                "boolean func1() {\n" +
                "    return true;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(func1() ? 3 : 2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(3));
    }

    @Test
    public void floatFunctionTest() {
        String code =
                "float func1() {\n" +
                "    return 123;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(func1() == 123 ? 3 : 2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(3));
    }

    @Test
    public void stringFunctionTest() {
        String code =
                "string func1() {\n" +
                "    return \"abc\";\n" +
                "}\n" +
                "\n" +
                "intStorage.add(func1() == \"abc\" ? 3 : 2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(3));
    }

    @Test
    public void arrayFunctionTest() {
        String code =
                "string[] func1() {\n" +
                "    string[] result = new string[3];\n" +
                "    result[0] = \"a\";\n" +
                "    result[1] = \"b\";\n" +
                "    result[2] = \"c\";\n" +
                "    return result;\n" +
                "}\n" +
                "\n" +
                "string[] array = func1();\n" +
                "intStorage.add(array.length);\n" +
                "intStorage.add(array[0] == \"a\" ? 5 : 0);\n" +
                "intStorage.add(array[1] == \"b\" ? 4 : 0);\n" +
                "intStorage.add(array[2] == \"c\" ? 3 : 0);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(3, 5, 4, 3));
    }

    @Test
    public void singleParamTest() {
        String code =
                "void func(int x) {\n" +
                "    intStorage.add(x + 1);\n" +
                "}\n" +
                "\n" +
                "for (int i = 0; i < 5; i++) {\n" +
                "    func(i);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(1, 2, 3, 4, 5));
    }

    @Test
    public void doubleParamTest() {
        String code =
                "int sum(int x, int y) {\n" +
                "    return x + y;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(sum(10, 10));\n" +
                "intStorage.add(sum(25, 15));\n" +
                "intStorage.add(sum(22, 12));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(20, 40, 34));
    }

    @Test
    public void implicitCastParamTest() {
        String code =
                "float sum(float x, float y) {\n" +
                "    return x + y;\n" +
                "}\n" +
                "\n" +
                "int a = 100;\n" +
                "floatStorage.add(sum(a, 10.5));\n" +
                "floatStorage.add(sum(25.5, a));\n" +
                "floatStorage.add(sum(22, 12));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                Lists.of(110.5, 125.5, 34.0));
    }

    @Test
    public void recursiveTest() {
        String code =
                "int factorial(int x) {\n" +
                "    if (x <= 1) {\n" +
                "        return 1;\n" +
                "    }\n" +
                "    return x * factorial(x - 1);\n" +
                "}\n" +
                "\n" +
                "intStorage.add(factorial(0));\n" +
                "intStorage.add(factorial(1));\n" +
                "intStorage.add(factorial(2));\n" +
                "intStorage.add(factorial(5));\n" +
                "intStorage.add(factorial(10));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(1, 1, 2, 120, 3628800));
    }

    @Test
    public void crossRecursionTest() {
        String code =
                "int strange(int x) {\n" +
                "    if (x <= 1) {\n" +
                "        return 1;\n" +
                "    }\n" +
                "    return 2 * func1(x - 1) + 3 * func2(x - 1);\n" +
                "}\n" +
                "\n" +
                "int func1(int x) {\n" +
                "    return strange(x - 2) + 2;\n" +
                "}\n" +
                "\n" +
                "int func2(int x) {\n" +
                "    return strange(x - 1) + 1;\n" +
                "}\n" +
                "\n" +
                "intStorage.add(strange(1));\n" +
                "intStorage.add(strange(2));\n" +
                "intStorage.add(strange(3));\n" +
                "intStorage.add(strange(4));\n" +
                "intStorage.add(strange(5));\n" +
                "intStorage.add(strange(6));\n" +
                "intStorage.add(strange(7));\n" +
                "intStorage.add(strange(8));\n" +
                "intStorage.add(strange(20));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(1, 12, 12, 45, 67, 166, 298, 639, 2563221));
    }

    @Test
    public void asLambdaTest1() {
        String code =
                "void func() {\n" +
                "    intStorage.add(25);\n" +
                "}\n" +
                "\n" +
                "run.once(func);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(25));
    }

    @Test
    public void asLambdaTest2() {
        String code =
                "void func(string s) {\n" +
                "    stringStorage.add(s + \"!\");\n" +
                "}\n" +
                "\n" +
                "run.onString(func);\n" +
                "run.triggerString(\"a\");\n" +
                "run.triggerString(\"b\");\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("a!", "b!"));
    }

    @Test
    public void asLambdaTest3() {
        String code =
                "void func(int i, string s) {\n" +
                "    intStorage.add(i + 1);\n" +
                "    stringStorage.add(s + \"!\");\n" +
                "}\n" +
                "\n" +
                "run.onIntString(func);\n" +
                "run.triggerIntString(10, \"a\");\n" +
                "run.triggerIntString(20, \"b\");\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(11, 21));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("a!", "b!"));
    }

    @Test
    public void asLambdaTest4() {
        String code =
                "int max(int i1, int i2) => i1 > i2 ? i1 : i2;\n" +
                "int max(int i1, int i2, int i3) => max(max(i1, i2), i3);\n" +
                "int max(int i1, int i2, int i3, int i4) => max(max(i1, i2), max(i3, i4));\n" +
                "void func1(int i1, int i2, fn<(int, int) => int> callback) => intStorage.add(callback(i1, i2));\n" +
                "void func2(int i1, int i2, int i3, fn<(int, int, int) => int> callback) => intStorage.add(callback(i1, i2, i3));\n" +
                "\n" +
                "func1(10, 9, max);\n" +
                "func2(5, 6, 4, max);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 6));
    }

    @Test
    public void refParameterTest1() {
        String code =
                "void inc(ref int x) {\n" +
                "    x++;\n" +
                "}\n" +
                "\n" +
                "int a = 100;\n" +
                "inc(ref a);\n" +
                "intStorage.add(a);\n" +
                "inc(ref a);\n" +
                "intStorage.add(a);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(101, 102));
    }

    @Test
    public void refParameterTest2() {
        String code =
                "void ten(ref float x) {\n" +
                "    x *= 10;\n" +
                "}\n" +
                "\n" +
                "float x = 1.0625;\n" +
                "for (int i = 0; i < 4; i++) {\n" +
                "    ten(ref x);\n" +
                "    floatStorage.add(x);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, Lists.of(10.625, 106.25, 1062.5, 10625.0));
    }

    @Test
    public void multiLayerRefTest() {
        String code =
                "void f1(ref int x) {\n" +
                "    x += 10;\n" +
                "}\n" +
                "void f2(ref int x) {\n" +
                "    x += 100;\n" +
                "    f1(ref x);\n" +
                "}\n" +
                "void f3(ref int x) {\n" +
                "    x += 1000;\n" +
                "    f2(ref x);\n" +
                "}\n" +
                "\n" +
                "int a = 1;\n" +
                "f3(ref a);\n" +
                "intStorage.add(a);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1111));
    }

    @Test
    public void parameterWithNameAsStaticConstantTest() {
        String code =
                "int f(int run) { return run * run; }\n" +
                "\n" +
                "intStorage.add(f(100));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10000));
    }

    @Test
    public void duplicateParameterTest() {
        String code =
                "void f(int x, string ⟦x⟧) {}\n";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.SymbolAlreadyDeclared,
                "x");
    }

    @Test
    public void letParameterTest() {
        String code =
                "void f(⟦let⟧ x) {}\n";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                ParserErrors.TypeExpected,
                "let");
    }

    @Test
    public void arrowFunctionTest1() {
        String code =
                "int sum(int i1, int i2) => i1 + i2;\n" +
                "void f1() => sum(1, 2);\n" +
                "void f2() => intStorage.add(120);\n" +
                "\n" +
                "intStorage.add(sum(100, 10));\n" +
                "f1();\n" +
                "f2();\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(110, 120));
    }

    @Test
    public void arrowFunctionTest2() {
        String code =
                "int sum(int i1, int i2) => ⟦intStorage.add(i1 + i2)⟧;\n";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.CannotImplicitlyConvert,
                "void", "int");
    }

    @Test
    public void staticVariableConflictTest() {
        String code =
                "static int func;\n" +
                "void ⟦func⟧(){}\n";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.SymbolAlreadyDeclared,
                "func");
    }

    @Test
    public void externalNameOverrideTest() {
        String code =
                "void run() {\n" +
                "    intStorage.add(123);\n" +
                "}\n" +
                "run();\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(123), ApiRoot.intStorage.list);
    }

    @Test
    public void typeAliasConflictTest() {
        String code =
                "typealias func = int;\n" +
                "void ⟦func⟧(){}\n";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.SymbolAlreadyDeclared,
                "func");
    }

    @Test
    public void classConflictTest() {
        String code =
                "class func {}\n" +
                "void ⟦func⟧(){}\n";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.SymbolAlreadyDeclared,
                "func");
    }

    @Test
    public void functionOverloadTest1() {
        String code =
                "int max(int i1, int i2) => i1 > i2 ? i1 : i2;\n" +
                "int max(int i1, int i2, int i3) => max(max(i1, i2), i3);\n" +
                "int max(int i1, int i2, int i3, int i4) => max(max(i1, i2), max(i3, i4));\n" +
                "\n" +
                "intStorage.add(max(1, 2));\n" +
                "intStorage.add(max(1, 3, 2));\n" +
                "intStorage.add(max(1, 4, 3, 2));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(2, 3, 4));
    }

    @Test
    public void functionOverloadTest2() {
        String code =
                "int max(int i1, int i2) => i1 > i2 ? i1 : i2;\n" +
                "int max(int i1, int i2, int i3) => max(max(i1, i2), i3);\n" +
                "int max(int i1, int i2, int i3, int i4) => max(max(i1, i2), max(i3, i4));\n" +
                "\n" +
                "intStorage.add(⟦max⟧(1));\n";

        String candidates =
                "Candidates:\n" +
                "int max(int i1, int i2)\n" +
                "int max(int i1, int i2, int i3)\n" +
                "int max(int i1, int i2, int i3, int i4)";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.NoOverloadedFunctions,
                "max", 1, candidates);
    }

    @Test
    public void functionOverloadTest3() {
        String code =
                "int max(int i1, int i2) => i1 > i2 ? i1 : i2;\n" +
                "\n" +
                "intStorage.add(⟦max⟧(1));\n";

        String candidates =
                "Candidates:\n" +
                "int max(int i1, int i2)";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.FunctionArgumentCountMismatch,
                "max", 2, candidates);
    }

    @Test
    public void functionOverloadTest4() {
        String code =
                "void func(int i1, int i2) {}\n" +
                "int ⟦func⟧(int a1, int a2) => 0;\n";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.FunctionAlreadyDeclared);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
        public static Run run;
    }
}