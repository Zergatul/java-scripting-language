package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClassNamePrefixTests {

    @Test
    public void basicTest() {
        Compiler compiler = new Compiler(new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .setMainClassName("TestScript")
                .build());
        CompilationResult result = compiler.compile("");
        Assertions.assertNull(result.getDiagnostics());
        Assertions.assertEquals("com.zergatul.scripting.dynamic.TestScript", result.getProgram().getClass().getName());
    }

    public static class ApiRoot { }
}