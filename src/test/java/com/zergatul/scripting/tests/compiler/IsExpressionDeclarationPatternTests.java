package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

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
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "Object x = \"hello\";\n" +
                "if (x is string str) {\n" +
                "    stringStorage.add(str);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("hello"));
    }

    @Test
    public void ifStatementTest2() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "Object x = \"hello\";\n" +
                "if (x is not string str) {\n" +
                "} else {\n" +
                "    stringStorage.add(str);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("hello"));
    }

    @Test
    public void ifStatementTest3() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "Object x = \"hello\";\n" +
                "if (!(x is string str)) {\n" +
                "} else {\n" +
                "    stringStorage.add(str);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("hello"));
    }

    @Test
    public void ifStatementTest4() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "Object x = \"hello\";\n" +
                "if (!!(x is string str)) {\n" +
                "    stringStorage.add(str);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("hello"));
    }

    @Test
    public void ifStatementAndTest1() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "Object o1 = \"hello\";\n" +
                "Object o2 = 123;\n" +
                "if (o1 is string str && o2 is int i) {\n" +
                "    stringStorage.add(str);\n" +
                "    intStorage.add(i);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(123));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("hello"));
    }

    @Test
    public void ifStatementOrTest1() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "Object o1 = \"hello\";\n" +
                "Object o2 = 123;\n" +
                "if (o1 is not string str || o2 is not int i) {\n" +
                "} else {\n" +
                "    stringStorage.add(str);\n" +
                "    intStorage.add(i);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(123));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("hello"));
    }

    @Test
    public void ifStatementScopeTest1() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "Object x = \"hello\";\n" +
                "if (x is string str) {\n" +
                "} else {\n" +
                "    stringStorage.add(str);\n" +
                "}\n";

        comparator.assertEquals(
                Lists.of(
                        new DiagnosticMessage(
                                BinderErrors.NameDoesNotExist,
                                new SingleLineTextRange(6, 23, 118, 3),
                                "str")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void ifStatementScopeTest2() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "Object x = \"hello\";\n" +
                "if (x is not string str) {\n" +
                "    stringStorage.add(str);\n" +
                "} else {\n" +
                "}\n";

        comparator.assertEquals(
                Lists.of(
                        new DiagnosticMessage(
                                BinderErrors.NameDoesNotExist,
                                new SingleLineTextRange(5, 23, 113, 3),
                                "str")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void ifStatementAndScopeTest1() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "Object o1 = \"hello\";\n" +
                "Object o2 = 123;\n" +
                "if (o1 is not string str && o2 is not int i) {\n" +
                "    stringStorage.add(str);\n" +
                "    intStorage.add(i);\n" +
                "}\n";

        comparator.assertEquals(
                Lists.of(
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
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "Object o1 = \"hello\";\n" +
                "Object o2 = 123;\n" +
                "if (o1 is string str || o2 is int i) {\n" +
                "    stringStorage.add(str);\n" +
                "    intStorage.add(i);\n" +
                "}\n";

        comparator.assertEquals(
                Lists.of(
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
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "Object x = \"hello\";\n" +
                "if (x is not string str) {\n" +
                "    return;\n" +
                "}\n" +
                "stringStorage.add(str);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("hello"));
    }

    @Test
    public void ifStatementVariableFallthroughTest2() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "Object x = \"hello\";\n" +
                "if (x is string str) {\n" +
                "    stringStorage.add(str);\n" +
                "} else {\n" +
                "    return;\n" +
                "}\n" +
                "stringStorage.add(str);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("hello", "hello"));
    }

    @Test
    public void ifStatementVariableFallthroughScopeTest1() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "Object x = \"hello\";\n" +
                "if (x is string str) {\n" +
                "    return;\n" +
                "}\n" +
                "stringStorage.add(str);\n";

        comparator.assertEquals(
                Lists.of(
                        new DiagnosticMessage(
                                BinderErrors.NameDoesNotExist,
                                new SingleLineTextRange(7, 19, 119, 3),
                                "str")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void ifStatementVariableFallthroughScopeTest2() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "Object x = \"hello\";\n" +
                "if (x is not string str) {\n" +
                "} else {\n" +
                "    return;\n" +
                "}\n" +
                "stringStorage.add(str);\n";

        comparator.assertEquals(
                Lists.of(
                        new DiagnosticMessage(
                                BinderErrors.NameDoesNotExist,
                                new SingleLineTextRange(8, 19, 132, 3),
                                "str")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void asyncTest1() {
        String code =
                "await futures.create();\n" +
                "let x = \"123\";\n" +
                "if (x is string str) {\n" +
                "    stringStorage.add(str);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertTrue(ApiRoot.stringStorage.list.isEmpty());

        ApiRoot.futures.get(0).complete(null);

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("123"));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void asyncTest2() {
        String code =
                "await futures.create();\n" +
                "let x = \"123\";\n" +
                "if (x is string str) {\n" +
                "    await futures.create();\n" +
                "    stringStorage.add(str);\n" +
                "}\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertTrue(ApiRoot.stringStorage.list.isEmpty());

        ApiRoot.futures.get(0).complete(null);

        Assertions.assertTrue(ApiRoot.stringStorage.list.isEmpty());

        ApiRoot.futures.get(1).complete(null);

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("123"));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void asyncTest3() {
        String code =
                "let x = \"123\";\n" +
                "if (x is not string str) {\n" +
                "    await futures.create();\n" +
                "    return;\n" +
                "}\n" +
                "await futures.create();\n" +
                "stringStorage.add(str);\n";

        AsyncRunnable program = compileAsync(ApiRoot.class, code);
        CompletableFuture<?> future = program.run();

        Assertions.assertTrue(ApiRoot.stringStorage.list.isEmpty());

        ApiRoot.futures.get(0).complete(null);

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("123"));
        Assertions.assertTrue(future.isDone());
    }

    @Test
    public void redeclarationTest1() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "int str = 1;\n" +
                "Object x = \"hello\";\n" +
                "if (x is string str) {}\n";

        comparator.assertEquals(
                Lists.of(
                        new DiagnosticMessage(
                                BinderErrors.SymbolAlreadyDeclared,
                                new SingleLineTextRange(5, 17, 93, 3),
                                "str")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void redeclarationTest2() {
        String code =
                "typealias Object = Java<java.lang.Object>;\n" +
                "\n" +
                "Object o1 = \"hello\";\n" +
                "Object o2 = 1;\n" +
                "if (o1 is string str && o2 is int str) {}\n";

        comparator.assertEquals(
                Lists.of(
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