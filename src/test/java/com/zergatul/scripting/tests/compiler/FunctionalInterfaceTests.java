package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.compiler.helpers.Run;
import com.zergatul.scripting.tests.compiler.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class FunctionalInterfaceTests {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.run = new Run();
    }

    @Test
    public void classMethodAsRunnableTest() {
        String code = """
                class MyClass {
                    int value;
                    constructor(int value) { this.value = value; }
                    void add() { intStorage.add(this.value); this.value++; }
                }
                
                let c = new MyClass(5);
                run.multiple(3, c.add);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(5, 6, 7));
    }

    @Test
    public void classMethodAsActionParametrizedTest1() {
        String code = """
                class MyClass {
                    int value;
                    constructor(int value) { this.value = value; }
                    void add(string str) { stringStorage.add(this.value + ". " + str); }
                }
                
                let c = new MyClass(1);
                run.onString(c.add);
                stringStorage.add("pre");
                run.triggerString("Duck");
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("pre", "1. Duck"));
    }

    @Test
    public void classMethodAsActionParametrizedTest2() {
        String code = """
                class MyClass {
                    int value;
                    constructor(int value) { this.value = value; }
                    void add(int x) { intStorage.add(this.value * x); }
                }
                
                let c = new MyClass(5);
                run.onInteger(c.add);
                intStorage.add(777);
                run.triggerInteger(10);
                run.triggerInteger(20);
                run.triggerInteger(30);
                """;

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(777, 50, 100, 150));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static Run run;
    }
}