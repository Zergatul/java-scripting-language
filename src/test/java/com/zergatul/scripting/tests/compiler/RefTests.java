package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Test;

public class RefTests extends ComparatorTest {

    @Test
    public void invalidRefVariableTest() {
        String code = """
                long[] a = new long[45];
                long.tryParse("123", ⟦ref a⟧);
                """;

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.RefTypeNotSupported,
                "long[]");
    }

    @Test
    public void refTypeMismatchTest() {
        String code = """
                float32 f;
                float.tryParse⟦("123", ref f)⟧;
                """;

        String candidates = """
                Candidates:
                boolean tryParse(string str, ref float result)""";

        comparator.assertDiagnostics(
                ApiRoot.class, code, "⟦⟧",
                BinderErrors.MethodInvalidArguments,
                "tryParse", candidates);
    }

    public static class ApiRoot {}
}