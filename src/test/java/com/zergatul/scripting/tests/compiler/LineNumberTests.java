package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.AsyncRunnable;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.tests.compiler.helpers.*;
import com.zergatul.scripting.type.SVoidType;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class LineNumberTests {

    @BeforeEach
    public void clean() {
        ApiRoot.lambdas = new LambdasApi();
        ApiRoot.futures = new FutureHelper();
    }

    @Test
    public void simpleTest() {
        Runnable program = compile(
                "let array = [1, 2, 3];\n" +
                "array[3] = 0;\n");

        try {
            program.run();
        } catch (ArrayIndexOutOfBoundsException exception) {
            assertStackTrace(exception, Lists.of(
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run", "<TestScript>", 2)));
            return;
        }

        Assertions.fail();
    }

    @Test
    public void functionTest() {
        Runnable program = compile(
                "void foo(int[] arr) {\n" +
                "    arr[3] = 0;\n" +
                "}\n" +
                "\n" +
                "let array = [1, 2, 3];\n" +
                "foo(array);\n");

        try {
            program.run();
        } catch (ArrayIndexOutOfBoundsException exception) {
            assertStackTrace(exception, Lists.of(
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "foo", "<TestScript>", 2),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run", "<TestScript>", 6)));
            return;
        }

        Assertions.fail();
    }

    @Test
    public void recursionTest() {
        Runnable program = compile(
                "int sum(int[] array, int index) {\n" +
                "    return array[index] + sum(array, index + 1);\n" +
                "}\n" +
                "\n" +
                "sum([1, 2, 3, 4, 5], 0);\n");

        try {
            program.run();
        } catch (ArrayIndexOutOfBoundsException exception) {
            assertStackTrace(exception, Lists.of(
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "sum", "<TestScript>", 2),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "sum", "<TestScript>", 2),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "sum", "<TestScript>", 2),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "sum", "<TestScript>", 2),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "sum", "<TestScript>", 2),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "sum", "<TestScript>", 2),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run", "<TestScript>", 5)));
            return;
        }

        Assertions.fail();
    }

    @Test
    public void lambdaTest1() {
        Runnable program = compile(
                "let array = [1, 2, 3, 4, 5];\n" +
                "lambdas.reduce(array, 0, 6, 0, (accumulator, current) => {\n" +
                "    return accumulator + current;\n" +
                "});\n");

        try {
            program.run();
        } catch (ArrayIndexOutOfBoundsException exception) {
            assertStackTrace(exception, Lists.of(
                    new StackTraceElement("com.zergatul.scripting.tests.compiler.LineNumberTests$LambdasApi", "reduce", "LineNumberTests.java", 0),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run", "<TestScript>", 2)));
            return;
        }

        Assertions.fail();
    }

    @Test
    public void lambdaTest2() {
        Runnable program = compile(
                "let array = [1, 2, 3, 4, 5];\n" +
                "lambdas.iterate(0, 6, (index) => {\n" +
                "    array[index]++;\n" +
                "});\n");

        try {
            program.run();
        } catch (ArrayIndexOutOfBoundsException exception) {
            assertStackTrace(exception, Lists.of(
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run$lambda$3", "<TestScript>", 3),
                    new StackTraceElement("com.zergatul.scripting.dynamic.DynamicLambdaClass_3", "accept", "<TestScript>", -1),
                    new StackTraceElement("com.zergatul.scripting.tests.compiler.LineNumberTests$LambdasApi", "iterate", "LineNumberTests.java", 0),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run", "<TestScript>", 2)));
            return;
        }

        Assertions.fail();
    }

    @Test
    public void lambdaInFunctionTest() {
        Runnable program = compile(
                "void fail() {\n" +
                "    let array = [1, 2, 3];\n" +
                "    lambdas.iterate(0, 4, index => {\n" +
                "        array[index]++;\n" +
                "    });\n" +
                "}\n" +
                "\n" +
                "fail();\n");

        try {
            program.run();
        } catch (ArrayIndexOutOfBoundsException exception) {
            assertStackTrace(exception, Lists.of(
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "fail$lambda$1", "<TestScript>", 4),
                    new StackTraceElement("com.zergatul.scripting.dynamic.DynamicLambdaClass_1", "accept", "<TestScript>", -1),
                    new StackTraceElement("com.zergatul.scripting.tests.compiler.LineNumberTests$LambdasApi", "iterate", "LineNumberTests.java", 0),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "fail", "<TestScript>", 3),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run", "<TestScript>", 8)));
            return;
        }

        Assertions.fail();
    }

    @Test
    public void lambdaInClassMethodTest() {
        Runnable program = compile(
                "class Class {\n" +
                "    void fail() {\n" +
                "        let array = [1, 2, 3];\n" +
                "        lambdas.iterate(0, 4, index => {\n" +
                "            array[index]++;\n" +
                "        });\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "new Class().fail();\n");

        try {
            program.run();
        } catch (ArrayIndexOutOfBoundsException exception) {
            assertStackTrace(exception, Lists.of(
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script$Class", "fail$lambda$1", null, 5),
                    new StackTraceElement("com.zergatul.scripting.dynamic.DynamicLambdaClass_1", "accept", "<TestScript>", -1),
                    new StackTraceElement("com.zergatul.scripting.tests.compiler.LineNumberTests$LambdasApi", "iterate", "LineNumberTests.java", 0),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script$Class", "fail", null, 4),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run", "<TestScript>", 10)));
            return;
        }

        Assertions.fail();
    }

    @Test
    public void lambdaInConstructorTest() {
        Runnable program = compile(
                "class Class {\n" +
                "    constructor() {\n" +
                "        let array = [1, 2, 3];\n" +
                "        lambdas.iterate(0, 4, index => {\n" +
                "            array[index]++;\n" +
                "        });\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "new Class();\n");

        try {
            program.run();
        } catch (ArrayIndexOutOfBoundsException exception) {
            assertStackTrace(exception, Lists.of(
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script$Class", "constructor$lambda$1", null, 5),
                    new StackTraceElement("com.zergatul.scripting.dynamic.DynamicLambdaClass_1", "accept", "<TestScript>", -1),
                    new StackTraceElement("com.zergatul.scripting.tests.compiler.LineNumberTests$LambdasApi", "iterate", "LineNumberTests.java", 0),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script$Class", "<init>", null, 4),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run", "<TestScript>", 10)));
            return;
        }

        Assertions.fail();
    }

    @Test
    public void lambdaInStaticInitializerTest() {
        Runnable program = compile(
                "static fn<int => void> fail = index => {\n" +
                "    let array = [1, 2, 3];\n" +
                "    array[index]++;\n" +
                "};\n" +
                "\n" +
                "fail(3);\n");

        try {
            program.run();
        } catch (ArrayIndexOutOfBoundsException exception) {
            assertStackTrace(exception, Lists.of(
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "staticInitializer$lambda$1", "<TestScript>", 3),
                    new StackTraceElement("com.zergatul.scripting.dynamic.DynamicLambdaClass_1", "apply", "<TestScript>", -1),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run", "<TestScript>", 6)));
            return;
        }

        Assertions.fail();
    }

    @Test
    public void asyncTest() {
        String code =
                "let array = [1, 2, 3, 4, 5, 6, 7];\n" +
                "while (true) {\n" +
                "    int value = await futures.createInt();\n" +
                "    if (value == 100) {\n" +
                "        break;\n" +
                "    }\n" +
                "    array[value]++;\n" +
                "}\n";
        AsyncRunnable program = compileAsync(code);

        Future<?> future = program.run();

        for (int i = 0; i <= 7; i++) {
            ApiRoot.futures.getInt(i).complete(i);
        }

        try {
            future.get();
        } catch (ExecutionException exception) {
            assertStackTrace(exception.getCause(), Lists.of(
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run$async$next$1", "<TestScript>", 7)));
            return;
        } catch (Throwable ignored) {}

        Assertions.fail();
    }

    @Test
    public void asyncFunctionTest() {
        String code =
                "async void fail() {\n" +
                "    let array = [1, 2, 3];\n" +
                "    await futures.create();\n" +
                "    array[3]++;\n" +
                "}\n" +
                "\n" +
                "await fail();\n";
        AsyncRunnable program = compileAsync(code);

        Future<?> future = program.run();
        ApiRoot.futures.get(0).complete(null);

        try {
            future.get();
        } catch (ExecutionException exception) {
            assertStackTrace(exception.getCause(), Lists.of(
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "fail$async$next$1", "<TestScript>", 4)));
            return;
        } catch (Throwable ignored) {}

        Assertions.fail();
    }

    @Test
    public void asyncClassMethodTest() {
        String code =
                "class Class {\n" +
                "    async void fail() {\n" +
                "        let array = [1, 2, 3];\n" +
                "        await futures.create();\n" +
                "        array[3]++;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "await new Class().fail();\n";

        AsyncRunnable program = compileAsync(code);

        Future<?> future = program.run();
        ApiRoot.futures.get(0).complete(null);

        try {
            future.get();
        } catch (ExecutionException exception) {
            assertStackTrace(exception.getCause(), Lists.of(
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script$Class", "fail$async$next$1", null, 5)));
            return;
        } catch (Throwable ignored) {}

        Assertions.fail();
    }

    private static void assertStackTrace(Throwable throwable, List<StackTraceElement> expected) {
        StackTraceElement[] actual = throwable.getStackTrace();
        for (int i = 0; i < expected.size(); i++) {
            Assertions.assertTrue(
                    stackTraceElementsEqual(actual[i], expected.get(i)),
                    "Expected: " + expected.get(i) + ", actual: " + actual[i]);
        }
    }

    private static boolean stackTraceElementsEqual(StackTraceElement actual, StackTraceElement expected) {
        if (!Objects.equals(actual.getClassName(), expected.getClassName())) {
            boolean isDynamicLambda =
                    actual.getClassName().startsWith("com.zergatul.scripting.dynamic.DynamicLambdaClass_") &&
                    expected.getClassName().startsWith("com.zergatul.scripting.dynamic.DynamicLambdaClass_");
            boolean isStateMachine =
                    actual.getClassName().startsWith("com.zergatul.scripting.dynamic.DynamicAsyncStateMachine_") &&
                    expected.getClassName().startsWith("com.zergatul.scripting.dynamic.DynamicAsyncStateMachine_");
            if (!isDynamicLambda && !isStateMachine) {
                return false;
            }
        }
        if (!Objects.equals(actual.getMethodName(), expected.getMethodName())) {
            boolean isLambdaBody = generatedLambdaMethodNamesEqual(
                    actual.getMethodName(),
                    expected.getMethodName());
            if (!isLambdaBody) {
                return false;
            }
        }
        if (!Objects.equals(actual.getFileName(), expected.getFileName())) {
            return false;
        }
        if (!actual.getClassName().startsWith("com.zergatul.scripting.tests.compiler")) {
            return Objects.equals(actual.getLineNumber(), expected.getLineNumber());
        }
        return true;
    }

    private static boolean generatedLambdaMethodNamesEqual(String actual, String expected) {
        String marker = "$lambda$";
        int actualIndex = actual.lastIndexOf(marker);
        int expectedIndex = expected.lastIndexOf(marker);
        return actualIndex >= 0 &&
                expectedIndex >= 0 &&
                actual.substring(0, actualIndex).equals(expected.substring(0, expectedIndex));
    }

    private static Runnable compile(String code) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .setSourceFile("<TestScript>")
                .emitLineNumbers(true)
                //.setDebug()
                .build());
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.getDiagnostics());
        return result.getProgram();
    }

    public static AsyncRunnable compileAsync(String code) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .setInterface(AsyncRunnable.class)
                .setAsyncReturnType(SVoidType.instance)
                .setSourceFile("<TestScript>")
                .emitLineNumbers(true)
                //.setDebug()
                .build());
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.getDiagnostics());
        return result.getProgram();
    }

    public static class ApiRoot {
        public static LambdasApi lambdas = new LambdasApi();
        public static FutureHelper futures = new FutureHelper();
    }

    public static class LambdasApi {

        public int reduce(int[] array, int from, int to, int initial, BiFunction<Integer, Integer, Integer> reducer) {
            int result = initial;
            for (int i = from; i < to; i++) {
                result = reducer.apply(result, array[i]);
            }
            return result;
        }

        public void iterate(int from, int to, Consumer<Integer> consumer) {
            for (int i = 0; i < to; i++) {
                consumer.accept(i);
            }
        }
    }
}