package com.zergatul.scripting.tests.compiler.helpers;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class CompilerHelper {

    public static Runnable compile(Class<?> api, String code) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder().setRoot(api).build());
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.getDiagnostics());
        return result.getProgram();
    }

    public static List<DiagnosticMessage> getDiagnostics(Class<?> api, String code) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder().setRoot(api).build());
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.getProgram());
        return result.getDiagnostics();
    }
}