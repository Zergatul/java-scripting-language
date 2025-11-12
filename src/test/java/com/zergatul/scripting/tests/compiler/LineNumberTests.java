package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.AsyncRunnable;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.tests.compiler.helpers.*;
import com.zergatul.scripting.type.SVoidType;
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
        Runnable program = compile("""
                let array = [1, 2, 3];
                array[3] = 0;
                """);

        try {
            program.run();
        } catch (ArrayIndexOutOfBoundsException exception) {
            assertStackTrace(exception, List.of(
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run", "<TestScript>", 2)));
            return;
        }

        Assertions.fail();
    }

    @Test
    public void functionTest() {
        Runnable program = compile("""
                void foo(int[] arr) {
                    arr[3] = 0;
                }
                
                let array = [1, 2, 3];
                foo(array);
                """);

        try {
            program.run();
        } catch (ArrayIndexOutOfBoundsException exception) {
            assertStackTrace(exception, List.of(
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "foo", "<TestScript>", 2),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run", "<TestScript>", 6)));
            return;
        }

        Assertions.fail();
    }

    @Test
    public void recursionTest() {
        Runnable program = compile("""
                int sum(int[] array, int index) {
                    return array[index] + sum(array, index + 1);
                }
                
                sum([1, 2, 3, 4, 5], 0);
                """);

        try {
            program.run();
        } catch (ArrayIndexOutOfBoundsException exception) {
            assertStackTrace(exception, List.of(
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
        Runnable program = compile("""
                let array = [1, 2, 3, 4, 5];
                lambdas.reduce(array, 0, 6, 0, (accumulator, current) => {
                    return accumulator + current;
                });
                """);

        try {
            program.run();
        } catch (ArrayIndexOutOfBoundsException exception) {
            assertStackTrace(exception, List.of(
                    new StackTraceElement("com.zergatul.scripting.tests.compiler.LineNumberTests$LambdasApi", "reduce", "LineNumberTests.java", 0),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run", "<TestScript>", 2)));
            return;
        }

        Assertions.fail();
    }

    @Test
    public void lambdaTest2() {
        Runnable program = compile("""
                let array = [1, 2, 3, 4, 5];
                lambdas.iterate(0, 6, (index) => {
                    array[index]++;
                });
                """);

        try {
            program.run();
        } catch (ArrayIndexOutOfBoundsException exception) {
            assertStackTrace(exception, List.of(
                    new StackTraceElement("com.zergatul.scripting.dynamic.DynamicLambdaClass_3", "accept", "<TestScript>", 3),
                    new StackTraceElement("com.zergatul.scripting.tests.compiler.LineNumberTests$LambdasApi", "iterate", "LineNumberTests.java", 0),
                    new StackTraceElement("com.zergatul.scripting.dynamic.Script", "run", "<TestScript>", 2)));
            return;
        }

        Assertions.fail();
    }

    @Test
    public void asyncTest() {
        AsyncRunnable program = compileAsync("""
                let array = [1, 2, 3, 4, 5, 6, 7];
                while (true) {
                    int value = await futures.createInt();
                    if (value == 100) {
                        break;
                    }
                    array[value]++;
                }
                """);

        Future<?> future = program.run();

        for (int i = 0; i <= 7; i++) {
            ApiRoot.futures.getInt(i).complete(i);
        }

        try {
            future.get();
        } catch (ExecutionException exception) {
            assertStackTrace(exception.getCause(), List.of(
                    new StackTraceElement("com.zergatul.scripting.dynamic.DynamicAsyncStateMachine_2", "next", "<TestScript>", 7)));
            return;
        } catch (Throwable ignored) {}

        Assertions.fail();
    }

    private static void assertStackTrace(Throwable throwable, List<StackTraceElement> expected) {
        StackTraceElement[] actual = throwable.getStackTrace();
        for (int i = 0; i < expected.size(); i++) {
            Assertions.assertTrue(stackTraceElementsEqual(actual[i], expected.get(i)));
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
            return false;
        }
        if (!Objects.equals(actual.getFileName(), expected.getFileName())) {
            return false;
        }
        if (!actual.getClassName().startsWith("com.zergatul.scripting.tests.compiler")) {
            return Objects.equals(actual.getLineNumber(), expected.getLineNumber());
        }
        return true;
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