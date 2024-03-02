package com.zergatul.scripting.tests;

import com.zergatul.scripting.old.compiler.ScriptCompileException;
import com.zergatul.scripting.old.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class VariablesTest {

    @BeforeEach
    public void clean() {
        ApiRoot.intStorage = new IntStorage();
    }

    @Test
    public void sumTest() throws Exception {
        String code = """
                int x;
                x = x + 1;
                int y = 2;
                y = x + y;
                intStorage.add(x);
                intStorage.add(y);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(1, 3));
    }

    @Test
    public void cannotReuseIdentifierSimpleTest() {
        String code = """
                int x;
                int y = 2;
                int x = y;
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        assertThrows(ScriptCompileException.class, () -> compiler.compile(code));
    }

    @Test
    public void cannotReuseIdentifierNestedTest() {
        String code = """
                int x;
                if (x > 0) {
                    int x = 123;
                }
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        assertThrows(ScriptCompileException.class, () -> compiler.compile(code));
    }

    @Test
    public void reuseIdentifierInAnotherScopeTest() throws Exception {
        String code = """
                boolean b = true;
                if (b) {
                    int inner = 123;
                    intStorage.add(inner);
                }
                b = !b;
                if (!b) {
                    int inner = 456;
                    intStorage.add(inner);
                }
                int inner = 789;
                intStorage.add(inner);
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(123, 456, 789));
    }

    @Test
    public void rawBlocksTest() throws Exception {
        String code = """
                {
                    int x = 23;
                    intStorage.add(x);
                }
                {
                    int x = 45;
                    intStorage.add(x);
                }
                {
                    int x = 67;
                    intStorage.add(x);
                }
                """;

        ScriptingLanguageCompiler compiler = new ScriptingLanguageCompiler(ApiRoot.class);
        Runnable program = compiler.compile(code);
        program.run();

        Assertions.assertIterableEquals(
                ApiRoot.intStorage.list,
                List.of(23, 45, 67));
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}