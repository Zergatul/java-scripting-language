package com.zergatul.scripting.tests.compiler.helpers;

import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import org.junit.jupiter.api.Assertions;

public class CompilerHelper {
    public static Runnable compile(Class<?> api, String code) {
        Compiler compiler = new Compiler(new CompilationParameters(api));
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.diagnostics());
        return result.program();
    }
}