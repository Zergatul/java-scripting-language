package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

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
        String code =
                "int[] array = new int[10];\n" +
                "for (int i = 0; i < array.length; i++) {\n" +
                "    array[i] = (i + 1) * 10;\n" +
                "}\n" +
                "foreach (int x in array) {\n" +
                "    if (x > 50) {\n" +
                "        break;\n" +
                "    }\n" +
                "    intStorage.add(x);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(10, 20, 30, 40, 50));
    }

    @Test
    public void continueStatementTest()  {
        String code =
                "int[] array = new int[10];\n" +
                "for (int i = 0; i < array.length; i++) {\n" +
                "    array[i] = i + 1;\n" +
                "}\n" +
                "foreach (int x in array) {\n" +
                "    if (x % 2 == 0) {\n" +
                "        continue;\n" +
                "    }\n" +
                "    intStorage.add(x);\n" +
                "}\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                Lists.of(1, 3, 5, 7, 9));
    }

    @Test
    public void floatArrayTest() {
        String code =
                "float[] a = new float[] { 0.5, 1.5, 2.5 };\n" +
                "foreach (float f in a) floatStorage.add(f);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.floatStorage.list,
                Lists.of(0.5, 1.5, 2.5));
    }

    @Test
    public void variableContextTest() {
        String code =
                "foreach (let x in [1]) x.toString();\n" +
                "let a = x;\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(BinderErrors.NameDoesNotExist, new SingleLineTextRange(2, 9, 45, 1), "x")),
                getDiagnostics(ApiRoot.class, code));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
    }
}