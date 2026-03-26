package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.compiler.*;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class MethodUsagePolicyTests extends ComparatorTest {

    @Test
    public void basicTest() {
        String code = """
                boolStorage.add(true);
                intStorage.add(1);
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.MethodUsageNotAllowed,
                                new SingleLineTextRange(2, 12, 34, 3),
                                "No use int storage add!")),
                getDiagnostics(code, new MethodUsagePolicy() {
                    @NullMarked
                    @Override
                    public Optional<String> validate(Method method) {
                        if (method.getDeclaringClass() == IntStorage.class && method.getName().equals("add")) {
                            return Optional.of("No use int storage add!");
                        } else {
                            return Optional.empty();
                        }
                    }
                }));
    }

    private static List<DiagnosticMessage> getDiagnostics(String code, MethodUsagePolicy policy) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder().setRoot(ApiRoot.class).setPolicy(policy).build());
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.getProgram());
        return result.getDiagnostics();
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage = new BoolStorage();
        public static IntStorage intStorage = new IntStorage();
    }
}