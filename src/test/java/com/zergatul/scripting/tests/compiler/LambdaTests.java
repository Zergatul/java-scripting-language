package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.FloatStorage;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.Run;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class LambdaTests {

    @BeforeEach
    public void clean() {
        ApiRoot.run = new Run();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
    }

    @Test
    public void simpleTest() {
        String code = """
                run.skip(() => {
                    intStorage.add(20);
                });
                run.once(() => {
                    intStorage.add(10);
                    intStorage.add(5);
                });
                run.multiple(3, () => {
                    intStorage.add(2);
                });
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(10, 5, 2, 2, 2));
    }

    @Test
    public void noBlockTest() {
        String code = """
                run.once(() => intStorage.add(120));
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(120));
    }

    @Test
    public void noUnboxingTest() {
        String code = """
                run.onString(str => stringStorage.add("1. " + str));
                run.onString(str => stringStorage.add("2. " + str));
                run.triggerString("qwerty");
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("1. qwerty", "2. qwerty"));
    }

    @Test
    public void unboxBooleanTest() {
        String code = """
                run.onBoolean(b => intStorage.add(b ? 2 : 1));
                run.onBoolean(b => intStorage.add(b ? 5 : 4));
                run.triggerBoolean(false);
                run.triggerBoolean(true);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(1, 4, 2, 5));
    }

    @Test
    public void unboxIntTest() {
        String code = """
                run.onInteger(i => intStorage.add(i + 1));
                run.onInteger(i => intStorage.add(i + 2));
                run.triggerInteger(100);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(101, 102));
    }

    @Test
    public void unboxFloatTest() {
        String code = """
                run.onFloat(v => floatStorage.add(v + 0.5));
                run.onFloat(v => floatStorage.add(v + 0.25));
                run.triggerFloat(1);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, List.of(1.5, 1.25));
    }

    @Test
    public void twoParamsTest() {
        String code = """
                run.onIntString((i, s) => floatStorage.add(i + 0.5));
                run.onIntString((i, s) => stringStorage.add("$" + s));
                run.triggerIntString(1, "a");
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, List.of(1.5));
        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("$a"));
    }

    @Test
    public void returnContextTest() {
        String code = """
                int a = 123;
                run.once(() => intStorage.add(321));
                intStorage.add(a);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(321, 123));
    }

    public static class ApiRoot {
        public static Run run;
        public static IntStorage intStorage = new IntStorage();
        public static FloatStorage floatStorage = new FloatStorage();
        public static StringStorage stringStorage = new StringStorage();
    }
}