package com.zergatul.scripting.tests;

import com.zergatul.scripting.helpers.BoolStorage;
import com.zergatul.scripting.helpers.FloatStorage;
import com.zergatul.scripting.helpers.IntStorage;
import com.zergatul.scripting.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.helpers.Run;
import com.zergatul.scripting.helpers.StringStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class StaticVariablesTest {

    @BeforeEach
    public void clean() {
        ApiRoot.run = new Run();
        ApiRoot.boolStorage = new BoolStorage();
        ApiRoot.intStorage = new IntStorage();
        ApiRoot.floatStorage = new FloatStorage();
        ApiRoot.stringStorage = new StringStorage();
        ApiRoot.storage1 = new IntStorage();
        ApiRoot.storage2 = new IntStorage();
    }

    @Test
    public void booleanInitTest() throws Exception {
        String code = """
                static boolean b = true;
                
                run.once(() => {
                    boolStorage.add(b);
                });
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.boolStorage.list, List.of(true));
    }

    @Test
    public void intInitTest() throws Exception {
        String code = """
                static int i = 100;
                
                run.once(() => {
                    intStorage.add(i);
                });
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(100));
    }

    @Test
    public void floatInitTest() throws Exception {
        String code = """
                static float d = 1.25;
                
                run.once(() => {
                    floatStorage.add(d);
                });
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.floatStorage.list, List.of(1.25));
    }

    @Test
    public void stringInitTest() throws Exception {
        String code = """
                static string s = "qwerty";
                
                run.once(() => {
                    stringStorage.add(s);
                });
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.stringStorage.list, List.of("qwerty"));
    }

    @Test
    public void modifyTest() throws Exception {
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

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.intStorage.list, List.of(0, 100, 200, 201));
    }

    @Test
    public void initRunOnceTest() throws Exception {
        String code = """
                static int i1 = 1;
                static int i2 = i1 + 1;
                
                i1++;
                storage1.add(i1);
                storage2.add(i2);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();
        program.run();
        program.run();
        program.run();
        program.run();

        Assertions.assertIterableEquals(ApiRoot.storage1.list, List.of(2, 3, 4, 5, 6));
        Assertions.assertIterableEquals(ApiRoot.storage2.list, List.of(2, 2, 2, 2, 2));
    }

    public static class ApiRoot {
        public static Run run;
        public static BoolStorage boolStorage;
        public static IntStorage intStorage;
        public static FloatStorage floatStorage;
        public static StringStorage stringStorage;
        public static IntStorage storage1;
        public static IntStorage storage2;
    }
}