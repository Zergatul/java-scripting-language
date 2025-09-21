package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class RefTests extends ComparatorTest {

    @Test
    public void invalidRefVariableTest() {
        String code = """
                long[] a = new long[45];
                long.tryParse("123", ref a);
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(BinderErrors.RefTypeNotSupported, new SingleLineTextRange(2, 22, 46, 5), "int64[]")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void refTypeMismatchTest() {
        String code = """
                float32 f;
                float.tryParse("123", ref f);
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(BinderErrors.CannotCastArguments, new SingleLineTextRange(2, 15, 25, 14))),
                getDiagnostics(ApiRoot.class, code));
    }

    public static class ApiRoot {}
}