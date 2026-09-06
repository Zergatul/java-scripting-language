package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.AsyncRunnable;
import com.zergatul.scripting.Getter;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.analysis.Analyzer;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.tests.compiler.helpers.*;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.type.CustomType;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compileAsyncWithCustomTypes;

public class LambdaTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.run = new Run();
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void simpleTest() {
        String code =
                "run.skip(() => {\n" +
                "    intStorage.add(20);\n" +
                "});\n" +
                "run.once(() => {\n" +
                "    intStorage.add(10);\n" +
                "    intStorage.add(5);\n" +
                "});\n" +
                "run.multiple(3, () => {\n" +
                "    intStorage.add(2);\n" +
                "});\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 5, 2, 2, 2));
    }

    @Test
    public void noBlock1Test() {
        String code =
                "run.once(() => intStorage.add(120));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(120));
    }

    @Test
    public void noBlock2Test() {
        String code =
                "static int x = 100;\n" +
                "run.once(() => x = 200);\n" +
                "intStorage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(200));
    }

    @Test
    public void noBlock3Test() {
        String code =
                "static int x = 100;\n" +
                "run.once(() => x++);\n" +
                "intStorage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(101));
    }

    @Test
    public void noBlock4Test() {
        String code =
                "static int x = 100;\n" +
                "run.once(() => x--);\n" +
                "intStorage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(99));
    }

    @Test
    public void noBlock5Test() {
        String code =
                "static int x = 100;\n" +
                "run.once(() => x *= 3);\n" +
                "intStorage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(300));
    }

    @Test
    public void noUnboxingTest() {
        String code =
                "run.onString(str => stringStorage.add(\"1. \" + str));\n" +
                "run.onString(str => stringStorage.add(\"2. \" + str));\n" +
                "run.triggerString(\"qwerty\");\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("1. qwerty", "2. qwerty"));
    }

    @Test
    public void unboxBooleanTest() {
        String code =
                "run.onBoolean(b => intStorage.add(b ? 2 : 1));\n" +
                "run.onBoolean(b => intStorage.add(b ? 5 : 4));\n" +
                "run.triggerBoolean(false);\n" +
                "run.triggerBoolean(true);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(1, 4, 2, 5));
    }

    @Test
    public void unboxIntTest() {
        String code =
                "run.onInteger(i => intStorage.add(i + 1));\n" +
                "run.onInteger(i => intStorage.add(i + 2));\n" +
                "run.triggerInteger(100);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(101, 102));
    }

    @Test
    public void unboxFloatTest() {
        String code =
                "run.onFloat(v => floatStorage.add(v + 0.5));\n" +
                "run.onFloat(v => floatStorage.add(v + 0.25));\n" +
                "run.triggerFloat(1);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, Lists.of(1.5, 1.25));
    }

    @Test
    public void twoParamsTest() {
        String code =
                "run.onIntString((i, s) => floatStorage.add(i + 0.5));\n" +
                "run.onIntString((i, s) => stringStorage.add(\"$\" + s));\n" +
                "run.triggerIntString(1, \"a\");\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, Lists.of(1.5));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("$a"));
    }

    @Test
    public void returnContextTest() {
        String code =
                "int a = 123;\n" +
                "run.once(() => intStorage.add(321));\n" +
                "intStorage.add(a);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(321, 123));
    }

    @Test
    public void simpleFunctionTest() {
        String code =
                "intStorage.add(run.sumInts(10, () => 10));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(100));
    }

    @Test
    public void simpleFunctionCastTest() {
        String code =
                "floatStorage.add(run.sumFloats(5, () => 10));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, Lists.of(50.0));
    }

    @Test
    public void simpleFunctionCannotCastTest() {
        String code =
                "run.sumInts(123, () => ⟦123.0⟧);\n";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.CannotImplicitlyConvert,
                "float", "Boxed<int>");
    }

    @Test
    public void map1Test() {
        String code =
                "int[] array = run.map(new int[] { 1, 2, 3, 4, 5 }, i => i * 2);\n" +
                "foreach (int i in array) intStorage.add(i);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(2, 4, 6, 8, 10));
    }

    @Test
    public void map2Test() {
        String code =
                "int x2(int value) { return value * 2; }\n" +
                "\n" +
                "int[] array = run.map(new int[] { 1, 2, 3, 4, 5 }, x2);\n" +
                "foreach (int i in array) intStorage.add(i);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(2, 4, 6, 8, 10));
    }

    @Test
    public void map3Test() {
        String code =
                "int[] array = run.map(new int[] { 1, 2, 3, 4, 5 }, i => { return i * 2; });\n" +
                "foreach (int i in array) intStorage.add(i);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(2, 4, 6, 8, 10));
    }

    @Test
    public void reduce1Test() {
        String code =
                "int result = run.reduce(new int[] { 1, 2, 3, 4, 5 }, 100, (acc, value) => acc + value * value);\n" +
                "intStorage.add(result);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(155));
    }

    @Test
    public void reduce2Test() {
        String code =
                "int func(int acc, int value) { return acc + value * value; }\n" +
                "\n" +
                "int result = run.reduce(new int[] { 1, 2, 3, 4, 5 }, 100, func);\n" +
                "intStorage.add(result);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(155));
    }

    @Test
    public void captureInt1Test() {
        String code =
                "int a = 100;\n" +
                "a++;\n" +
                "intStorage.add(a);\n" +
                "run.once(() => a = 200);\n" +
                "intStorage.add(a);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(101, 200));
    }

    @Test
    public void captureInt2Test() {
        String code =
                "int a = 100;\n" +
                "run.once(() => {\n" +
                "    run.once(() => {\n" +
                "        a = 200;\n" +
                "    });\n" +
                "});\n" +
                "intStorage.add(a);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(200));
    }

    @Test
    public void captureInt3Test() {
        String code =
                "int a = 100;\n" +
                "run.once(() => {\n" +
                "    int b = 200;\n" +
                "    run.once(() => {\n" +
                "        a += b;\n" +
                "    });\n" +
                "});\n" +
                "intStorage.add(a);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(300));
    }

    @Test
    public void captureInt4Test() {
        String code =
                "int a = 1;\n" +
                "int b = 2;\n" +
                "int c = 3;\n" +
                "int d = 4;\n" +
                "int e = 5;\n" +
                "run.once(() => {\n" +
                "    a++;\n" +
                "    run.once(() => {\n" +
                "        b++;\n" +
                "        run.once(() => {\n" +
                "            c++;\n" +
                "            run.once(() => {\n" +
                "                d++;\n" +
                "                run.once(() => {\n" +
                "                    e++;\n" +
                "                });\n" +
                "            });\n" +
                "        });\n" +
                "    });\n" +
                "});\n" +
                "intStorage.add(a);\n" +
                "intStorage.add(b);\n" +
                "intStorage.add(c);\n" +
                "intStorage.add(d);\n" +
                "intStorage.add(e);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(2, 3, 4, 5, 6));
    }

    @Test
    public void captureInt5Test() {
        String code =
                "int x;\n" +
                "run.multiple(1, () => {\n" +
                "    run.multiple(2, () => {\n" +
                "        run.multiple(3, () => {\n" +
                "            run.multiple(4, () => {\n" +
                "                run.multiple(5, () => {\n" +
                "                    x++;\n" +
                "                });\n" +
                "            });\n" +
                "        });\n" +
                "    });\n" +
                "});\n" +
                "intStorage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(120));
    }

    @Test
    public void captureInt6Test() {
        String code =
                "int x1 = 1;\n" +
                "run.multiple(1, () => {\n" +
                "    int x2 = 2;\n" +
                "    run.multiple(2, () => {\n" +
                "        int x3 = 3;\n" +
                "        run.multiple(3, () => {\n" +
                "            int x4 = 4;\n" +
                "            run.multiple(4, () => {\n" +
                "                int x5 = 5;\n" +
                "                run.multiple(5, () => {\n" +
                "                    x1++;\n" +
                "                    x2++;\n" +
                "                    x3++;\n" +
                "                    x4++;\n" +
                "                    x5++;\n" +
                "                });\n" +
                "                x1 += x5;\n" +
                "            });\n" +
                "            x1 += x4;\n" +
                "        });\n" +
                "        x1 += x3;\n" +
                "    });\n" +
                "    x1 += x2;\n" +
                "});\n" +
                "intStorage.add(x1);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(753));
    }

    @Test
    public void captureInt7Test() {
        String code =
                "int ticks = 1;\n" +
                "run.multiple(ticks, () => {\n" +
                "    run.multiple(ticks, () => {\n" +
                "        run.multiple(ticks, () => {\n" +
                "            run.multiple(ticks, () => {\n" +
                "                run.multiple(ticks, () => {\n" +
                "                    intStorage.add(ticks + ticks);\n" +
                "                });\n" +
                "            });\n" +
                "        });\n" +
                "    });\n" +
                "});\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(2));
    }

    @Test
    public void captureFloatTest() {
        String code =
                "float x;\n" +
                "run.multiple(1, () => {\n" +
                "    run.multiple(2, () => {\n" +
                "        run.multiple(3, () => {\n" +
                "            run.multiple(4, () => {\n" +
                "                run.multiple(5, () => {\n" +
                "                    x = x + 1;\n" +
                "                });\n" +
                "            });\n" +
                "        });\n" +
                "    });\n" +
                "});\n" +
                "floatStorage.add(x);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, Lists.of(120.0));
    }

    @Test
    public void captureBooleanTest() {
        String code =
                "boolean b;\n" +
                "run.multiple(1, () => {\n" +
                "    run.multiple(3, () => {\n" +
                "        run.multiple(5, () => {\n" +
                "            run.multiple(7, () => {\n" +
                "                run.multiple(9, () => {\n" +
                "                    b = !b;\n" +
                "                });\n" +
                "            });\n" +
                "        });\n" +
                "    });\n" +
                "});\n" +
                "boolStorage.add(b);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true));
    }

    @Test
    public void captureMultipleTest() {
        String code =
                "int a1 = 1;\n" +
                "int a2 = 2;\n" +
                "int a3 = 3;\n" +
                "int sum;\n" +
                "run.once(() => {\n" +
                "    int b1 = 4;\n" +
                "    int b2 = 5;\n" +
                "    int b3 = 6;\n" +
                "    run.once(() => {\n" +
                "        sum += a1;\n" +
                "        sum += a2;\n" +
                "        sum += a3;\n" +
                "        sum += b1;\n" +
                "        sum += b2;\n" +
                "        sum += b3;\n" +
                "    });\n" +
                "});\n" +
                "intStorage.add(sum);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(21));
    }

    @Test
    public void customFunctionalInterface1Test() {
        String code =
                "custom.test(id => intStorage.add(id));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(100, 101, 102, 103, 104));
    }

    @Test
    public void customFunctionalInterface2Test() {
        String code =
                "custom.test((i1, d1, i2, d2, i3, d3) => {\n" +
                "    intStorage.add(i1 + i2 + i3);\n" +
                "    floatStorage.add(d1 + d2 + d3);\n" +
                "});\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(33));
        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, Lists.of(0.875));
    }

    @Test
    public void genericConsumerIntTest() {
        String code =
                "custom.testPredicate(str => true);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true));
    }

    @Test
    public void predicateTest() {
        String code =
                "custom.acceptInt(i => intStorage.add(i));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(10, 11, 12, 13, 14));
    }

    @Test
    public void predicateAsyncTest() {
        String code =
                "custom.testSomething(10, instance => instance.value == 15);\n";

        AsyncRunnable program = compileAsyncWithCustomTypes(ApiRoot.class, code, ClassA.class);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(0, 1, 2, 3, 4, 5));
    }

    @Test
    public void localVariableShadowTest() {
        String code =
                "int x = 100;\n" +
                "fn<int => int> add = a => {\n" +
                "    int x = 7;\n" +
                "    return a + x;\n" +
                "};\n" +
                "intStorage.add(add(5));\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(12));
    }

    @Test
    public void failedArguments1Test() {
        String code =
                "run.⟦once⟧(10, () => {});\n";

        String candidates =
                "Candidates:\n" +
                "void once(Java<java.lang.Runnable> runnable)";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.NoOverloadedMethods,
                "once", 2, candidates);
    }

    @Test
    public void failedArguments2Test() {
        String code =
                "run.multiple⟦(\"10\", () => {})⟧;\n";

        String candidates =
                "Candidates:\n" +
                "void multiple(int count, Java<java.lang.Runnable> runnable)";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.MethodInvalidArguments,
                "multiple", candidates);
    }

    @Test
    public void failedArguments3Test() {
        String code =
                "run.multiple⟦(10, (x) => {})⟧;\n";

        String candidates =
                "Candidates:\n" +
                "void multiple(int count, Java<java.lang.Runnable> runnable)";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.MethodInvalidArguments,
                "multiple", candidates);
    }

    @Test
    public void failedArgument4Test() {
        String code =
                "run.multiple(10, (x) => {});\n";

        BinderOutput binderOutput = new Analyzer()
                .analyze(code, new CompilationParametersBuilder().setRoot(ApiRoot.class).build())
                .binderOutput();

        List<BoundStatementNode> statements = binderOutput.unit().statements.statements;
        Assertions.assertEquals(1, statements.size());

        BoundExpressionStatementNode statement = (BoundExpressionStatementNode) binderOutput.unit().statements.statements.get(0);
        BoundMethodInvocationExpressionNode invocation = (BoundMethodInvocationExpressionNode) statement.expression;
        List<BoundExpressionNode> arguments = invocation.arguments.arguments;
        Assertions.assertEquals(2, arguments.size());

        /*Assertions.assertEquals(arguments.get(0),
                new BoundIntegerLiteralExpressionNode(10, new SingleLineTextRange(1, 14, 13, 2)));*/

        Assertions.assertTrue(arguments.get(1) instanceof BoundUnconvertedLambdaExpressionNode);

        BoundUnconvertedLambdaExpressionNode lambda = (BoundUnconvertedLambdaExpressionNode) arguments.get(1);
        Assertions.assertEquals(new SingleLineTextRange(1, 18, 17, 9), lambda.getRange());
    }

    // TODO: capture function parameters?
    // maybe not allow!

    public static class ApiRoot {
        public static Run run;
        public static BoolStorage boolStorage = new BoolStorage();
        public static IntStorage intStorage = new IntStorage();
        public static FloatStorage floatStorage = new FloatStorage();
        public static StringStorage stringStorage = new StringStorage();
        public static Custom custom = new Custom();
    }

    public static class Custom {
        public void test(EntityIdConsumer consumer) {
            for (int i = 100; i < 105; i++) {
                consumer.accept(i);
            }
        }

        public void test(MultipleConsumer consumer) {
            consumer.accept(10, 0.5, 11, 0.25, 12, 0.125);
        }

        public void acceptInt(Consumer<Integer> consumer) {
            for (int i = 10; i < 15; i++) {
                consumer.accept(i);
            }
        }

        public void acceptFloat(Consumer<Double> consumer) {
            for (double d = 10; d < 11; d += 0.25) {
                consumer.accept(d);
            }
        }

        public void acceptString(Consumer<String> consumer) {
            for (String s = ""; s.length() < 5; s += "w") {
                consumer.accept(s);
            }
        }

        public void testPredicate(StringPredicate predicate) {
            ApiRoot.boolStorage.add(predicate.test("qwerty"));
        }

        public void testSomething(int value, CustomTypePredicate predicate) {
            for (int i = 0; i < 16; i++) {
                ApiRoot.intStorage.add(i);
                if (predicate.test(new ClassA(value + i))) {
                    return;
                }
            }
        }
    }

    @CustomType(name = "ClassA")
    public static class ClassA {

        private final int value;

        public ClassA(int value) {
            this.value = value;
        }

        @Getter(name = "value")
        public int getValue() {
            return value;
        }
    }

    @FunctionalInterface
    public interface EntityIdConsumer {
        void accept(int entityId);
    }

    @FunctionalInterface
    public interface MultipleConsumer {
        void accept(int i1, double d1, int i2, double d2, int i3, double d3);
    }

    @FunctionalInterface
    public interface StringPredicate {
        boolean test(String value);
    }

    @FunctionalInterface
    public interface CustomTypePredicate {
        boolean test(ClassA a);
    }
}