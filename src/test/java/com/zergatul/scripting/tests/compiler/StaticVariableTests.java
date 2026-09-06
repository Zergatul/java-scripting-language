package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.parser.ParserErrors;
import com.zergatul.scripting.tests.compiler.helpers.*;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;
import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.getDiagnostics;

public class StaticVariableTests extends ComparatorTest {

    @BeforeEach
    public void clean() {
        ApiRoot.run = new Run();
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.storage1 = new IntStorage();
        ApiRoot.storage2 = new IntStorage();
        ApiRoot.storage3 = new IntStorage();
    }

    @Test
    public void booleanInitTest() {
        String code =
                "static boolean b = true;\n" +
                "\n" +
                "run.once(() => {\n" +
                "    boolStorage.add(b);\n" +
                "});\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, Lists.of(true));
    }

    @Test
    public void intInitTest() {
        String code =
                "static int i = 100;\n" +
                "\n" +
                "run.once(() => {\n" +
                "    intStorage.add(i);\n" +
                "});\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(100));
    }

    @Test
    public void floatInitTest() {
        String code =
                "static float d = 1.25;\n" +
                "\n" +
                "run.once(() => {\n" +
                "    floatStorage.add(d);\n" +
                "});\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, Lists.of(1.25));
    }

    @Test
    public void stringInitTest() {
        String code =
                "static string s = \"qwerty\";\n" +
                "\n" +
                "run.once(() => {\n" +
                "    stringStorage.add(s);\n" +
                "});\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("qwerty"));
    }

    @Test
    public void modifyTest() {
        String code =
                "static int i;\n" +
                "\n" +
                "intStorage.add(i);\n" +
                "i = i + 100;\n" +
                "intStorage.add(i);\n" +
                "\n" +
                "run.once(() => {\n" +
                "    i = i + 100;\n" +
                "});\n" +
                "intStorage.add(i);\n" +
                "\n" +
                "run.once(() => {\n" +
                "    i++;\n" +
                "});\n" +
                "intStorage.add(i);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(0, 100, 200, 201));
    }

    @Test
    public void persistentTest() {
        String code =
                "static int i1 = 1;\n" +
                "static int i2 = i1 + 1;\n" +
                "\n" +
                "i1++;\n" +
                "storage1.add(i1);\n" +
                "storage2.add(i2);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();
        program.run();
        program.run();
        program.run();
        program.run();

        Assertions.assertIterableEquals(ApiRoot.storage1.list, Lists.of(2, 3, 4, 5, 6));
        Assertions.assertIterableEquals(ApiRoot.storage2.list, Lists.of(2, 2, 2, 2, 2));
    }

    @Test
    public void withFunctionsTest() {
        String code =
                "static int i1 = 1;\n" +
                "static int i2 = i1 + 1;\n" +
                "int square(int x) { return x * x; }\n" +
                "static int i3 = square(i3) + square(i1) + square(i2);\n" +
                "\n" +
                "i1++;\n" +
                "storage1.add(i1);\n" +
                "storage2.add(i2);\n" +
                "storage3.add(i3);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();
        program.run();

        Assertions.assertIterableEquals(ApiRoot.storage1.list, Lists.of(2, 3));
        Assertions.assertIterableEquals(ApiRoot.storage2.list, Lists.of(2, 2));
        Assertions.assertIterableEquals(ApiRoot.storage3.list, Lists.of(5, 5));
    }

    @Test
    public void letTest() {
        String code =
                "static let x = 1;\n";

        comparator.assertEquals(Lists.of(
                new DiagnosticMessage(ParserErrors.TypeExpected, new SingleLineTextRange(1, 8, 7, 3), "let")),
                getDiagnostics(ApiRoot.class, code));
    }

    @Test
    public void externalNameOverrideTest() {
        String code =
                "static int run = 123;\n" +
                "intStorage.add(run);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(Lists.of(123), ApiRoot.intStorage.list);
    }

    public static class ApiRoot {
        public static Run run;
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
        public static IntStorage storage1;
        public static IntStorage storage2;
        public static IntStorage storage3;
    }
}