package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import com.zergatul.scripting.type.SType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class JavaClassesTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.class3 = new Class3();
    }

    @Test
    public void inheritedMethodTest() {
        String code = """
                class3.do1(5);
                class3.do2(5);
                class3.do3(5);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(5, 15, 105));
    }

    @Test
    public void cannotUseObjectMethodsTest() {
        String code = """
                class3.notify();
                class3.getClass();
                class3.toString();
                class3.hashCode();
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(
                        BinderErrors.MemberDoesNotExist,
                        new SingleLineTextRange(1, 8, 7, 6),
                        SType.fromJavaType(Class3.class),
                        "notify"),
                new DiagnosticMessage(
                        BinderErrors.MemberDoesNotExist,
                        new SingleLineTextRange(2, 8, 24, 8),
                        SType.fromJavaType(Class3.class),
                        "getClass"),
                new DiagnosticMessage(
                        BinderErrors.MemberDoesNotExist,
                        new SingleLineTextRange(3, 8, 43, 8),
                        SType.fromJavaType(Class3.class),
                        "toString"),
                new DiagnosticMessage(
                        BinderErrors.MemberDoesNotExist,
                        new SingleLineTextRange(4, 8, 62, 8),
                        SType.fromJavaType(Class3.class),
                        "hashCode")),
                getDiagnostics(ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static Class3 class3;
    }

    public static class Class3 extends Class2 {
        public void do3(int value) {
            ApiRoot.intStorage.add(100 + value);
        }
    }

    public static class Class2 extends Class1 {
        public void do2(int value) {
            ApiRoot.intStorage.add(10 + value);
        }
    }

    public static class Class1 {
        public void do1(int value) {
            ApiRoot.intStorage.add(value);
        }
    }
}