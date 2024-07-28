package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.compiler.VisibilityChecker;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.type.SType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

public class VisibilityCheckerTests {

    @Test
    public void shouldNotAllowToUseHiddenMethodTest() {
        String code = """
                intStorage.add(0);
                """;

        List<DiagnosticMessage> messages = getDiagnostics(code, new VisibilityChecker() {
            @Override
            public boolean isVisible(Method method) {
                return !method.getName().equals("add");
            }
        });

        Assertions.assertIterableEquals(messages, List.of(
                new DiagnosticMessage(
                        BinderErrors.MemberDoesNotExist,
                        new SingleLineTextRange(1, 12, 11, 3),
                        SType.fromJavaType(IntStorage.class),
                        "add")));
    }

    /*private static Runnable compile(Class<?> api, String code, VisibilityChecker checker) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder().setRoot(api).setVisibilityChecker(checker).build());
        CompilationResult<Runnable> result = compiler.compile(code, Runnable.class);
        Assertions.assertNull(result.diagnostics());
        return result.program();
    }*/

    private static List<DiagnosticMessage> getDiagnostics(String code, VisibilityChecker checker) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder().setRoot(ApiRoot.class).setVisibilityChecker(checker).build());
        CompilationResult<Runnable> result = compiler.compile(code, Runnable.class);
        Assertions.assertNull(result.program());
        return result.diagnostics();
    }

    public static class ApiRoot {
        //public static BoolStorage boolStorage = new BoolStorage();
        public static IntStorage intStorage = new IntStorage();
    }
}