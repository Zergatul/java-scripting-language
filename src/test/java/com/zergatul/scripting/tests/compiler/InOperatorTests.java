package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class InOperatorTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void stringTest() {
        String code = """
                let str = "qwerty";
                boolStorage.add("q" in str);
                boolStorage.add("qwe" in str);
                boolStorage.add("qwerty" in str);
                boolStorage.add("a" in str);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true, true, true, false));
    }

    @Test
    public void containsNotDefinedTest() {
        String code = """
                boolStorage.add("" in 1);
                """;

        comparator.assertEquals(List.of(
                        new DiagnosticMessage(
                                BinderErrors.CannotUseInOperator,
                                new SingleLineTextRange(1, 17, 16, 7),
                                "int", "string")),
                getDiagnostics(ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
    }
}