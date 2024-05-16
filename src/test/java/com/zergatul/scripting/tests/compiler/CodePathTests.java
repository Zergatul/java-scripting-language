package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.binding.BinderErrors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class CodePathTests {

    @Test
    public void voidFunctionTest() {
        String code = """
                void func1() {}
                """;
        assertOk(code);
    }

    @Test
    public void noReturnZeroStatementsTest() {
        String code = """
                int func1() {}
                """;
        assertNotAllCodePaths(code);
    }

    @Test
    public void simpleReturnTest() {
        String code = """
                int func1() {
                    return 1;
                }
                """;
        assertOk(code);
    }

    @Test
    public void ifStatement1Test() {
        String code = """
                int func1() {
                    if (true) {
                        return 1;
                    } else {
                        return 2;
                    }
                }
                """;
        assertOk(code);
    }

    @Test
    public void ifStatement2Test() {
        String code = """
                int func1() {
                    if (true) {
                        return 1;
                    }
                }
                """;
        assertNotAllCodePaths(code);
    }

    @Test
    public void ifStatement3Test() {
        String code = """
                int func1() {
                    if (true) {
                        return 1;
                    } else {
                        func1();
                    }
                }
                """;
        assertNotAllCodePaths(code);
    }

    @Test
    public void ifStatement4Test() {
        String code = """
                int func1() {
                    if (true) {
                        func1();
                    } else {
                        return 1;
                    }
                }
                """;
        assertNotAllCodePaths(code);
    }

    private void assertOk(String code) {
        compile(ApiRoot.class, code);
    }

    private void assertNotAllCodePaths(String code) {
        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);
        Assertions.assertEquals(1, messages.size());
        Assertions.assertEquals(messages.get(0).code, BinderErrors.NotAllPathReturnValue.code());
    }

    public static final class ApiRoot {}
}