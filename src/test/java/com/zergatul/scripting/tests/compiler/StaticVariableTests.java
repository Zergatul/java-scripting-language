package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
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
        String code = """
                static boolean b = true;
                
                run.once(() => {
                    boolStorage.add(b);
                });
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true));
    }

    @Test
    public void intInitTest() {
        String code = """
                static int i = 100;
                
                run.once(() => {
                    intStorage.add(i);
                });
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(100));
    }

    @Test
    public void floatInitTest() {
        String code = """
                static float d = 1.25;
                
                run.once(() => {
                    floatStorage.add(d);
                });
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, List.of(1.25));
    }

    @Test
    public void stringInitTest() {
        String code = """
                static string s = "qwerty";
                
                run.once(() => {
                    stringStorage.add(s);
                });
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("qwerty"));
    }

    @Test
    public void modifyTest() {
        String code = """
                static int i;
                
                intStorage.add(i);
                i = i + 100;
                intStorage.add(i);
                
                run.once(() => {
                    i = i + 100;
                });
                intStorage.add(i);
                
                run.once(() => {
                    i++;
                });
                intStorage.add(i);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(0, 100, 200, 201));
    }

    @Test
    public void persistentTest() {
        String code = """
                static int i1 = 1;
                static int i2 = i1 + 1;
                
                i1++;
                storage1.add(i1);
                storage2.add(i2);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();
        program.run();
        program.run();
        program.run();
        program.run();

        Assertions.assertIterableEquals(ApiRoot.storage1.list, List.of(2, 3, 4, 5, 6));
        Assertions.assertIterableEquals(ApiRoot.storage2.list, List.of(2, 2, 2, 2, 2));
    }

    @Test
    public void withFunctionsTest() {
        String code = """
                static int i1 = 1;
                static int i2 = i1 + 1;
                int square(int x) { return x * x; }
                static int i3 = square(i3) + square(i1) + square(i2);
                
                i1++;
                storage1.add(i1);
                storage2.add(i2);
                storage3.add(i3);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();
        program.run();

        Assertions.assertIterableEquals(ApiRoot.storage1.list, List.of(2, 3));
        Assertions.assertIterableEquals(ApiRoot.storage2.list, List.of(2, 2));
        Assertions.assertIterableEquals(ApiRoot.storage3.list, List.of(5, 5));
    }

    @Test
    public void letTest() {
        String code = """
                static let x = 1;
                """;

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.TypeExpected, new SingleLineTextRange(1, 8, 7, 3), "let")),
                getDiagnostics(ApiRoot.class, code));
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