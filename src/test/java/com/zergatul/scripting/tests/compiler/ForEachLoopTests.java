package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class ForEachLoopTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
    }

    @Test
    public void breakStatementTest() {
        String code = """
                int[] array = new int[10];
                for (int i = 0; i < array.length; i++) {
                    array[i] = (i + 1) * 10;
                }
                foreach (int x in array) {
                    if (x > 50) {
                        break;
                    }
                    intStorage.add(x);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(10, 20, 30, 40, 50));
    }

    @Test
    public void continueStatementTest()  {
        String code = """
                int[] array = new int[10];
                for (int i = 0; i < array.length; i++) {
                    array[i] = i + 1;
                }
                foreach (int x in array) {
                    if (x % 2 == 0) {
                        continue;
                    }
                    intStorage.add(x);
                }
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1, 3, 5, 7, 9));
    }

    @Test
    public void floatArrayTest() {
        String code = """
                float[] a = new float[] { 0.5, 1.5, 2.5 };
                foreach (float f in a) floatStorage.add(f);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                List.of(0.5, 1.5, 2.5));
    }

    @Test
    public void variableContextTest() {
        String code = """
                foreach (let x in [1]) x.toString();
                let a = x;
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(BinderErrors.NameDoesNotExist, new SingleLineTextRange(2, 9, 45, 1), "x")),
                getDiagnostics(ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
    }
}