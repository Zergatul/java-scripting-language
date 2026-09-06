package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.utility.Lists;

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
        String code =
                "class MyClass {\n" +
                "    int value;\n" +
                "    constructor(int value) { this.value = value; }\n" +
                "    void add() { intStorage.add(this.value); this.value++; }\n" +
                "}\n" +
                "\n" +
                "let c = new MyClass(5);\n" +
                "run.multiple(3, c.add);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(5, 6, 7));
    }

    @Test
    public void classMethodAsActionParametrizedTest1() {
        String code =
                "class MyClass {\n" +
                "    int value;\n" +
                "    constructor(int value) { this.value = value; }\n" +
                "    void add(string str) { stringStorage.add(this.value + \". \" + str); }\n" +
                "}\n" +
                "\n" +
                "let c = new MyClass(1);\n" +
                "run.onString(c.add);\n" +
                "stringStorage.add(\"pre\");\n" +
                "run.triggerString(\"Duck\");\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, Lists.of("pre", "1. Duck"));
    }

    @Test
    public void classMethodAsActionParametrizedTest2() {
        String code =
                "class MyClass {\n" +
                "    int value;\n" +
                "    constructor(int value) { this.value = value; }\n" +
                "    void add(int x) { intStorage.add(this.value * x); }\n" +
                "}\n" +
                "\n" +
                "let c = new MyClass(5);\n" +
                "run.onInteger(c.add);\n" +
                "intStorage.add(777);\n" +
                "run.triggerInteger(10);\n" +
                "run.triggerInteger(20);\n" +
                "run.triggerInteger(30);\n";

        Runnable program = compile(ApiRoot.class, code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, Lists.of(777, 50, 100, 150));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static StringStorage stringStorage;
        public static Run run;
    }
}