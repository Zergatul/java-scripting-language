package com.zergatul.scripting.tests;

import com.zergatul.scripting.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.helpers.BoolStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;


public class UpCastsTest {

    @BeforeEach
    public void clean() {
        ApiRoot.boolStorage = new BoolStorage();
    }

    @Test
    public void intToFloatTest() throws Exception {
        String code = """
                boolStorage.add(1 + 0.5 == 1.5);
                boolStorage.add(0.5 + 1 == 1.5);
                boolStorage.add(1.5 - 0.5 == 1.0);
                boolStorage.add(1 - 0.5 == 0.5);
                
                boolStorage.add(1.5 * 2 == 3.0);
                boolStorage.add(2 * 1.5 == 3.0);
                boolStorage.add(3.0 / 2 == 1.5);
                boolStorage.add(3 / 2.0 == 1.5);
                boolStorage.add(3.0 % 2 == 1.0);
                boolStorage.add(3 % 2.0 == 1.0);
                
                boolStorage.add(1.9 < 2);
                boolStorage.add(2 < 2.1);
                boolStorage.add(2.1 > 2);
                boolStorage.add(2 > 1.9);
                
                boolStorage.add(1.9 <= 2);
                boolStorage.add(2.0 <= 2);
                boolStorage.add(2 <= 2.1);
                boolStorage.add(2 <= 2.0);
                boolStorage.add(2.1 >= 2);
                boolStorage.add(2.0 >= 2);
                boolStorage.add(2 >= 1.9);
                boolStorage.add(2 >= 2.0);
                
                boolStorage.add(2.0 == 2);
                boolStorage.add(2 == 2.0);
                boolStorage.add(2.1 != 2);
                boolStorage.add(2 != 2.1);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertEquals(ApiRoot.boolStorage.list.size(), 26);
        for (int i = 0; i < 26; i++) {
            Assertions.assertTrue(ApiRoot.boolStorage.list.get(i));
        }
    }

    @Test
    public void variableInitTest() throws Exception {
        String code = """
                float x = 123;
                boolStorage.add(x == 123);
                
                string str = 321;
                boolStorage.add(str == "321");
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.boolStorage.list,
                List.of(true, true));
    }

    public static class ApiRoot {
        public static BoolStorage boolStorage;
    }
}