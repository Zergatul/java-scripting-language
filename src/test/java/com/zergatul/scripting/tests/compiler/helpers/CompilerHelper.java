package com.zergatul.scripting.tests.compiler.helpers;

import com.zergatul.scripting.AsyncRunnable;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.type.SVoidType;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class CompilerHelper {

    public static Runnable compile(Class<?> api, String code) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder()
                .setRoot(api)
                .emitVariableNames(true)
                //.setDebug()
                .build());
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.getDiagnostics());
        return result.getProgram();
    }

    public static List<DiagnosticMessage> getDiagnostics(Class<?> api, String code) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder().setRoot(api).build());
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.getProgram());
        return result.getDiagnostics();
    }

    public static List<DiagnosticMessage> getDiagnostics(Class<?> api, String code, Class<?>... customTypes) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder()
                .setRoot(api)
                .addCustomTypes(Lists.of(customTypes))
                .build());
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.getProgram());
        return result.getDiagnostics();
    }

    public static AsyncRunnable compileAsync(Class<?> api, String code) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder()
                .setRoot(api)
                .setInterface(AsyncRunnable.class)
                .setAsyncReturnType(SVoidType.instance)
                .emitVariableNames(true)
                //.setDebug()
                .build());
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.getDiagnostics());
        return result.getProgram();
    }

    public static Throwable getExceptionNow(CompletableFuture<?> future) {
        if (!future.isDone() || !future.isCompletedExceptionally()) {
            throw new IllegalStateException("Future has not completed exceptionally.");
        }
        Throwable exception = future.handle((result, ex) -> ex).join();
        if (exception instanceof CompletionException) {
            CompletionException completionException = (CompletionException) exception;
            return completionException.getCause();
        } else {
            return exception;
        }
    }

    public static Runnable compileWithCustomType(Class<?> api, Class<?> custom, String code) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder()
                .setRoot(api)
                .addCustomType(custom)
                .emitVariableNames(true)
                //.setDebug()
                .build());
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.getDiagnostics());
        return result.getProgram();
    }

    public static Runnable compileWithCustomTypes(Class<?> api, String code, Class<?>... customTypes) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder()
                .setRoot(api)
                .addCustomTypes(Lists.of(customTypes))
                .emitVariableNames(true)
                //.setDebug()
                .build());
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.getDiagnostics());
        return result.getProgram();
    }

    public static AsyncRunnable compileAsyncWithCustomTypes(Class<?> api, String code, Class<?>... customTypes) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder()
                .setRoot(api)
                .addCustomTypes(Lists.of(customTypes))
                .setInterface(AsyncRunnable.class)
                .setAsyncReturnType(SVoidType.instance)
                .emitVariableNames(true)
                //.setDebug()
                .build());
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.getDiagnostics());
        return result.getProgram();
    }

    public static void assertTopStackTrace(List<StackTraceElement> expected, List<StackTraceElement> actual) {
        Assertions.assertNotNull(expected);
        Assertions.assertNotNull(actual);
        Assertions.assertTrue(expected.size() <= actual.size());
        for (int i = 0; i < expected.size(); i++) {
            StackTraceElement expectedElement = expected.get(i);
            StackTraceElement actualElement = actual.get(i);
            Assertions.assertEquals(expectedElement.getClassName(), actualElement.getClassName());
            Assertions.assertEquals(expectedElement.getMethodName(), actualElement.getMethodName());
            Assertions.assertEquals(expectedElement.getFileName(), actualElement.getFileName());
            Assertions.assertEquals(expectedElement.getLineNumber(), actualElement.getLineNumber());
        }
    }
}