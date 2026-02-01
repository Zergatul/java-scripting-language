package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class ExpressionTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void popExpressionReturnFromStackTest() {
        String code = """
                int get() {
                    return 123;
                }
                
                int count = 0;
                for (int i = 0; i < 1000000000; i++) {
                    get();
                    count++;
                }
                
                intStorage.add(count);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1000000000));
    }

    @Test
    public void staticReferenceAsExpressionTest1() {
        String code = """
                int x = int;
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.TypeReferenceNotAllowed,
                                new SingleLineTextRange(1, 9, 8, 3),
                                "int")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void staticReferenceAsExpressionTest2() {
        String code = """
                void func(int x, int y) {}
                func(int, Java<java.lang.Object>);
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.TypeReferenceNotAllowed,
                                new SingleLineTextRange(2, 6, 32, 3),
                                "int"),
                        new DiagnosticMessage(
                                BinderErrors.TypeReferenceNotAllowed,
                                new SingleLineTextRange(2, 11, 37, 22),
                                "Java<java.lang.Object>")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void staticReferenceAsExpressionTest3() {
        String code = """
                let a = #typeof(string);
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.TypeReferenceNotAllowed,
                                new SingleLineTextRange(1, 17, 16, 6),
                                "string")),
                getDiagnostics(ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}