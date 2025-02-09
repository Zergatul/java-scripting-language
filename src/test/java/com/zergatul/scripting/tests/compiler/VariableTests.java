package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.Run;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class VariableTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void sumTest() {
        String code = """
                int x;
                x = x + 1;
                int y = 2;
                y = x + y;
                intStorage.add(x);
                intStorage.add(y);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1, 3));
    }

    @Test
    public void cannotReuseIdentifierSimpleTest() {
        String code = """
                int x;
                int y = 2;
                int x = y;
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);
        Assertions.assertIterableEquals(messages, List.of(
                new DiagnosticMessage(BinderErrors.SymbolAlreadyDeclared, new SingleLineTextRange(3, 5, 22, 1), "x")));
    }

    @Test
    public void cannotReuseIdentifierNestedTest() {
        String code = """
                int x;
                if (x > 0) {
                    int x = 123;
                }
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);
        Assertions.assertIterableEquals(messages, List.of(
                new DiagnosticMessage(BinderErrors.SymbolAlreadyDeclared, new SingleLineTextRange(3, 9, 28, 1), "x")));
    }

    @Test
    public void reuseIdentifierInAnotherScopeTest() {
        String code = """
                boolean b = true;
                if (b) {
                    int inner = 123;
                    intStorage.add(inner);
                }
                b = !b;
                if (!b) {
                    int inner = 456;
                    intStorage.add(inner);
                }
                int inner = 789;
                intStorage.add(inner);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(123, 456, 789));
    }

    @Test
    public void rawBlocksTest() {
        String code = """
                {
                    int x = 23;
                    intStorage.add(x);
                }
                {
                    int x = 45;
                    intStorage.add(x);
                }
                {
                    int x = 67;
                    intStorage.add(x);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(23, 45, 67));
    }

    @Test
    public void variableAsStaticConstantTest() {
        String code = """
                let run = 123;
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);

        Assertions.assertIterableEquals(
                messages,
                List.of(new DiagnosticMessage(BinderErrors.SymbolAlreadyDeclared, new SingleLineTextRange(1, 5, 4, 3), "run")));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static Run run;
    }
}