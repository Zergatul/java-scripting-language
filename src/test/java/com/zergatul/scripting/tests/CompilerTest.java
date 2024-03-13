package com.zergatul.scripting.tests;

import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CompilerTest {

    public static final InternalAssertions assertions = new InternalAssertions();

    @Test
    public void test1() {
        var compiler = new Compiler(new CompilationParameters(CompilerTest.class));
        CompilationResult result = compiler.compile("int x = 1 + 2; assertions.isTrue(true);");
        Assertions.assertNull(result.diagnostics());
        result.program().run();
    }

    public static class InternalAssertions {

        public int fails;

        public void isTrue(boolean value) {
            if (!value) {
                fails++;
            }
        }

        public void reset() {
            fails = 0;
        }
    }
}