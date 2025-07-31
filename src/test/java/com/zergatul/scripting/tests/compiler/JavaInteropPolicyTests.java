package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.compiler.JavaInteropPolicy;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.type.SType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

public class JavaInteropPolicyTests {

    @Test
    public void shouldNotAllowToUseHiddenMethodTest() {
        String code = """
                intStorage.add(0);
                """;

        List<DiagnosticMessage> messages = getDiagnostics(code, new JavaInteropPolicy() {
            @Override
            public boolean isMethodVisible(Method method) {
                return !method.getName().equals("add");
            }

            @Override
            public boolean isJavaTypeUsageAllowed() {
                throw new AssertionError();
            }

            @Override
            public String getJavaTypeUsageError() {
                throw new AssertionError();
            }
        });

        Assertions.assertIterableEquals(messages, List.of(
                new DiagnosticMessage(
                        BinderErrors.MemberDoesNotExist,
                        new SingleLineTextRange(1, 12, 11, 3),
                        SType.fromJavaType(IntStorage.class),
                        "add")));
    }

    @Test
    public void javaTypeUsageNotAllowedTest() {
        String code = """
                Java<java.lang.Object> obj = new Java<java.lang.Object>();
                """;

        List<DiagnosticMessage> messages = getDiagnostics(code, new JavaInteropPolicy() {
            @Override
            public boolean isMethodVisible(Method method) {
                return true;
            }

            @Override
            public boolean isJavaTypeUsageAllowed() {
                return false;
            }

            @Override
            public String getJavaTypeUsageError() {
                return "error msg";
            }
        });

        Assertions.assertIterableEquals(messages, List.of(
                new DiagnosticMessage(
                        BinderErrors.JavaTypeNotAllowed,
                        new SingleLineTextRange(1, 1, 0, 22),
                        "error msg"),
                new DiagnosticMessage(
                        BinderErrors.JavaTypeNotAllowed,
                        new SingleLineTextRange(1, 34, 33, 22),
                        "error msg")));
    }

    private static List<DiagnosticMessage> getDiagnostics(String code, JavaInteropPolicy checker) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder().setRoot(ApiRoot.class).setPolicy(checker).build());
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.getProgram());
        return result.getDiagnostics();
    }

    public static class ApiRoot {
        //public static BoolStorage boolStorage = new BoolStorage();
        public static IntStorage intStorage = new IntStorage();
    }
}