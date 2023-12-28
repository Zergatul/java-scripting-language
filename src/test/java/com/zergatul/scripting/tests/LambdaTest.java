package com.zergatul.scripting.tests;

import com.zergatul.scripting.helpers.IntStorage;
import com.zergatul.scripting.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.helpers.Run;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class LambdaTest {

    @BeforeEach
    public void clean() {
        ApiRoot.run = new Run();
        ApiRoot.storage = new IntStorage();
    }

    @Test
    public void simpleTest() throws Exception {
        String code = """
                run.skip(() => {
                    storage.add(20);
                });
                run.once(() => {
                    storage.add(10);
                    storage.add(5);
                });
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(ApiRoot.storage.list, List.of(10, 5));
    }

    public static class ApiRoot {
        public static Run run;
        public static IntStorage storage = new IntStorage();
    }
}