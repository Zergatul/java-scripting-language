package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.InstanceCompilationResult;
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
        InstanceCompilationResult result = compiler.compileInstance("");
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("com.zergatul.scripting.dynamic.TestScript", result.getProgram().getClass().getName());
    }

    public static class ApiRoot { }
}