package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.parser.ParserErrors;
import com.zergatul.scripting.tests.compiler.helpers.BoolStorage;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class LetTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.storage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void letWithoutInitialization() {
        String code = """
                let x;
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.CannotUseLet, new SingleLineTextRange(1, 1, 0, 3))),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void intTest() {
        String code = """
                let a = 1;
                let b = 2;
                intStorage.add(a + b);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(3));
    }

    @Test
    public void floatTest() {
        String code = """
                let a = 1;
                let b = 2.5;
                floatStorage.add(a + b);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, List.of(3.5));
    }

    @Test
    public void stringTest() {
        String code = """
                let a = "qwe";
                stringStorage.add(a);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("qwe"));
    }

    @Test
    public void forLoopTest() {
        String code = """
                let sum = 0;
                for (let i = 0; i < 10; i++) sum += i;
                intStorage.add(sum);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(45));
    }

    @Test
    public void forEachLoopTest() {
        String code = """
                let array = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
                let sum = 0;
                foreach (let i in array) sum += i;
                intStorage.add(sum);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(45));
    }

    public static class ApiRoot {
        public static BoolStorage storage;
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
    }
}