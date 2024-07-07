package com.zergatul.scripting.tests.compiler.helpers;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class CompilerHelper {

    private static final boolean debug = false;

    public static Runnable compile(Class<?> api, String code) {
        Compiler compiler = new Compiler(new CompilationParameters(api, debug));
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.diagnostics());
        return result.program();
    }

    public static List<DiagnosticMessage> getDiagnostics(Class<?> api, String code) {
        Compiler compiler = new Compiler(new CompilationParameters(api, debug));
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.program());
        return result.diagnostics();
    }
}