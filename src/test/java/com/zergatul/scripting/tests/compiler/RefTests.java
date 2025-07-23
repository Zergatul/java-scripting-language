package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class RefTests {

    @Test
    public void invalidRefVariableTest() {
        String code = """
                long[] a = new long[45];
                long.tryParse("123", ref a);
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);

        Assertions.assertIterableEquals(
                messages,
                List.of(
                        new DiagnosticMessage(BinderErrors.RefTypeNotSupported, new SingleLineTextRange(2, 22, 46, 5), "int64[]")));
    }

    @Test
    public void refTypeMismatchTest() {
        String code = """
                float32 f;
                float.tryParse("123", ref f);
                """;

        List<DiagnosticMessage> messages = getDiagnostics(ApiRoot.class, code);

        Assertions.assertIterableEquals(
                messages,
                List.of(
                        new DiagnosticMessage(BinderErrors.CannotCastArguments, new SingleLineTextRange(2, 15, 25, 14))));
    }

    public static class ApiRoot {}
}