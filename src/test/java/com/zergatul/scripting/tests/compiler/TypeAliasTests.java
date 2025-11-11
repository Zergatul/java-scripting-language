package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class TypeAliasTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void variableTest() {
        String code = """
                typealias Int = int;
                
                Int i = 123;
                intStorage.add(i);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(123));
    }

    @Test
    public void parameterTest() {
        String code = """
                typealias str = string;
                str double(str s) => s + s;
                
                stringStorage.add(double("x"));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("xx"));
    }

    @Test
    public void javaTypeTest() {
        String code = """
                typealias Class1 = Java<com.zergatul.scripting.tests.compiler.TypeAliasTests$Class1>;
                typealias Class2 = Java<com.zergatul.scripting.tests.compiler.TypeAliasTests$Class2>;
                
                let c1 = new Class1();
                let c2 = new Class2();
                stringStorage.add(c1.getValue() + c2.getValue());
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("Value1Value2"));
    }

    @Test
    public void selfReferenceTest() {
        String code = """
                typealias MyType = MyType;
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.TypeAliasLoop, new SingleLineTextRange(1, 1, 0, 26))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void circularReferenceTest() {
        String code = """
                typealias MyType1 = MyType2;
                typealias MyType2 = MyType1;
                """;

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(BinderErrors.TypeAliasLoop, new SingleLineTextRange(2, 1, 29, 28))),
                getDiagnostics(ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }

    @SuppressWarnings("unused")
    public static class Class1 {
        public String getValue() {
            return "Value1";
        }
    }

    @SuppressWarnings("unused")
    public static class Class2 {
        public String getValue() {
            return "Value2";
        }
    }
}