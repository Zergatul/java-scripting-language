package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.Run;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class VariableTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void sumTest() {
        String code =
                "int x;\n" +
                "x = x + 1;\n" +
                "int y = 2;\n" +
                "y = x + y;\n" +
                "intStorage.add(x);\n" +
                "intStorage.add(y);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(1, 3));
    }

    @Test
    public void cannotReuseIdentifierSimpleTest() {
        String code =
                "int x;\n" +
                "int y = 2;\n" +
                "int x = y;\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.SymbolAlreadyDeclared, new SingleLineTextRange(3, 5, 22, 1), "x")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void cannotReuseIdentifierNestedTest() {
        String code =
                "int x;\n" +
                "if (x > 0) {\n" +
                "    int x = 123;\n" +
                "}\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.SymbolAlreadyDeclared, new SingleLineTextRange(3, 9, 28, 1), "x")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void reuseIdentifierInAnotherScopeTest() {
        String code =
                "boolean b = true;\n" +
                "if (b) {\n" +
                "    int inner = 123;\n" +
                "    intStorage.add(inner);\n" +
                "}\n" +
                "b = !b;\n" +
                "if (!b) {\n" +
                "    int inner = 456;\n" +
                "    intStorage.add(inner);\n" +
                "}\n" +
                "int inner = 789;\n" +
                "intStorage.add(inner);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(123, 456, 789));
    }

    @Test
    public void rawBlocksTest() {
        String code =
                "{\n" +
                "    int x = 23;\n" +
                "    intStorage.add(x);\n" +
                "}\n" +
                "{\n" +
                "    int x = 45;\n" +
                "    intStorage.add(x);\n" +
                "}\n" +
                "{\n" +
                "    int x = 67;\n" +
                "    intStorage.add(x);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(23, 45, 67));
    }

    @Test
    public void variableAsStaticConstantTest() {
        String code =
                "let run = 123;\n" +
                "intStorage.add(run);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(123));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static Run run;
    }
}