package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.AsyncRunnable;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.FutureHelper;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.*;

public class IsExpressionDeclarationPatternTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.futures = new FutureHelper();
    }

    @Test
    public void ifStatementTest1() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object x = "hello";
                if (x is string str) {
                    stringStorage.add(str);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("hello"));
    }

    @Test
    public void ifStatementTest2() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object x = "hello";
                if (x is not string str) {
                } else {
                    stringStorage.add(str);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("hello"));
    }

    @Test
    public void ifStatementTest3() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object x = "hello";
                if (!(x is string str)) {
                } else {
                    stringStorage.add(str);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("hello"));
    }

    @Test
    public void ifStatementTest4() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object x = "hello";
                if (!!(x is string str)) {
                    stringStorage.add(str);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("hello"));
    }

    @Test
    public void ifStatementAndTest1() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object o1 = "hello";
                Object o2 = 123;
                if (o1 is string str && o2 is int i) {
                    stringStorage.add(str);
                    intStorage.add(i);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("hello"));
    }

    @Test
    public void ifStatementOrTest1() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object o1 = "hello";
                Object o2 = 123;
                if (o1 is not string str || o2 is not int i) {
                } else {
                    stringStorage.add(str);
                    intStorage.add(i);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("hello"));
    }

    @Test
    public void ifStatementScopeTest1() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object x = "hello";
                if (x is string str) {
                } else {
                    stringStorage.add(str);
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.NameDoesNotExist,
                                new SingleLineTextRange(6, 23, 118, 3),
                                "str")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void ifStatementScopeTest2() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object x = "hello";
                if (x is not string str) {
                    stringStorage.add(str);
                } else {
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.NameDoesNotExist,
                                new SingleLineTextRange(5, 23, 113, 3),
                                "str")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void ifStatementAndScopeTest1() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object o1 = "hello";
                Object o2 = 123;
                if (o1 is not string str && o2 is not int i) {
                    stringStorage.add(str);
                    intStorage.add(i);
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.NameDoesNotExist,
                                new SingleLineTextRange(6, 23, 151, 3),
                                "str"),
                        new DiagnosticMessage(
                                BinderErrors.NameDoesNotExist,
                                new SingleLineTextRange(7, 20, 176, 1),
                                "i")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void ifStatementOrScopeTest1() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object o1 = "hello";
                Object o2 = 123;
                if (o1 is string str || o2 is int i) {
                    stringStorage.add(str);
                    intStorage.add(i);
                }
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.NameDoesNotExist,
                                new SingleLineTextRange(6, 23, 143, 3),
                                "str"),
                        new DiagnosticMessage(
                                BinderErrors.NameDoesNotExist,
                                new SingleLineTextRange(7, 20, 168, 1),
                                "i")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void ifStatementVariableFallthroughTest1() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object x = "hello";
                if (x is not string str) {
                    return;
                }
                stringStorage.add(str);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("hello"));
    }

    @Test
    public void ifStatementVariableFallthroughTest2() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object x = "hello";
                if (x is string str) {
                    stringStorage.add(str);
                } else {
                    return;
                }
                stringStorage.add(str);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("hello", "hello"));
    }

    @Test
    public void ifStatementVariableFallthroughScopeTest1() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object x = "hello";
                if (x is string str) {
                    return;
                }
                stringStorage.add(str);
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.NameDoesNotExist,
                                new SingleLineTextRange(7, 19, 119, 3),
                                "str")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void ifStatementVariableFallthroughScopeTest2() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object x = "hello";
                if (x is not string str) {
                } else {
                    return;
                }
                stringStorage.add(str);
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.NameDoesNotExist,
                                new SingleLineTextRange(8, 19, 132, 3),
                                "str")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void asyncTest1() {
        String code = """
                await futures.create();
                let x = "123";
                if (x is string str) {
                    stringStorage.add(str);
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertTrue(ApiRoot.stringStorage.list.isEmpty());

        ApiRoot.futures.get(0).complete(null);

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("123"));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void asyncTest2() {
        String code = """
                await futures.create();
                let x = "123";
                if (x is string str) {
                    await futures.create();
                    stringStorage.add(str);
                }
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertTrue(ApiRoot.stringStorage.list.isEmpty());

        ApiRoot.futures.get(0).complete(null);

        Assertions.assertTrue(ApiRoot.stringStorage.list.isEmpty());

        ApiRoot.futures.get(1).complete(null);

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("123"));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void asyncTest3() {
        String code = """
                let x = "123";
                if (x is not string str) {
                    await futures.create();
                    return;
                }
                await futures.create();
                stringStorage.add(str);
                """;

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertTrue(ApiRoot.stringStorage.list.isEmpty());

        ApiRoot.futures.get(0).complete(null);

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("123"));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void redeclarationTest1() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                int str = 1;
                Object x = "hello";
                if (x is string str) {}
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.SymbolAlreadyDeclared,
                                new SingleLineTextRange(5, 17, 93, 3),
                                "str")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void redeclarationTest2() {
        String code = """
                typealias Object = Java<java.lang.Object>;
                
                Object o1 = "hello";
                Object o2 = 1;
                if (o1 is string str && o2 is int str) {}
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.VariableRedeclarationInCondition,
                                new SingleLineTextRange(5, 5, 84, 33),
                                "str")),
                getDiagnostics(ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static FutureHelper futures;
    }
}