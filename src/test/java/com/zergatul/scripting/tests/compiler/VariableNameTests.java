package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VariableNameTests {

    @Test
    public void unitVariableTest() {
        String code =
                "let str = api.getNull();\n" +
                "str = str.substring(10);\n";
        Runnable program = compile(code);
        Assertions.assertThrows(NullPointerException.class, program::run);
    }

    @Test
    public void functionVariableTest() {
        String code =
                "void func() {\n" +
                "    string x = api.getNull();\n" +
                "    x.substring(10);\n" +
                "}\n" +
                "\n" +
                "func();\n";
        Runnable program = compile(code);
        Assertions.assertThrows(NullPointerException.class, program::run);
    }

    @Test
    public void functionParameterTest() {
        String code =
                "void func(string abc) {\n" +
                "    abc.substring(10);\n" +
                "}\n" +
                "\n" +
                "func(api.getNull());\n";
        Runnable program = compile(code);
        Assertions.assertThrows(NullPointerException.class, program::run);
    }

    @Test
    public void variableOverlayTest1() {
        String code =
                "string getString(boolean isNull) => isNull ? api.getNull() : \"\";\n" +
                "\n" +
                "{\n" +
                "    string str1 = getString(true);\n" +
                "    str1.substring(0);\n" +
                "}\n" +
                "{\n" +
                "    string str2 = getString(false);\n" +
                "    str2.substring(0);\n" +
                "}\n";
        Runnable program = compile(code);
        Assertions.assertThrows(NullPointerException.class, program::run);
    }

    @Test
    public void variableOverlayTest2() {
        String code =
                "string getString(boolean isNull) => isNull ? api.getNull() : \"\";\n" +
                "\n" +
                "{\n" +
                "    string str1 = getString(false);\n" +
                "    str1.substring(0);\n" +
                "}\n" +
                "{\n" +
                "    string str2 = getString(true);\n" +
                "    str2.substring(0);\n" +
                "}\n";

        Runnable program = compile(code);
        Assertions.assertThrows(NullPointerException.class, program::run);
    }

    @Test
    public void forLoopTest() {
        String code =
                "let array = [api.getNull()];\n" +
                "for (let i = 0; i < array.length; i++) {\n" +
                "    let element = array[i];\n" +
                "    element.substring(0);\n" +
                "}\n";
        Runnable program = compile(code);
        Assertions.assertThrows(NullPointerException.class, program::run);
    }

    @Test
    public void forEachLoopTest() {
        String code =
                "let array = [api.getNull()];\n" +
                "foreach (let element in array) {\n" +
                "    element.substring(0);\n" +
                "}\n";
        Runnable program = compile(code);
        Assertions.assertThrows(NullPointerException.class, program::run);
    }

    @Test
    public void whileLoopTest() {
        String code =
                "let myStr = api.getNull();\n" +
                "while (true) {\n" +
                "    myStr.substring(0);\n" +
                "}\n";
        Runnable program = compile(code);
        Assertions.assertThrows(NullPointerException.class, program::run);
    }

    @Test
    public void lambdaTest() {
        String code =
                "void process(fn<string => void> func) => func(api.getNull());\n" +
                "\n" +
                "process(sss => sss.substring(10));\n";
        Runnable program = compile(code);
        Assertions.assertThrows(NullPointerException.class, program::run);
    }

    private static Runnable compile(String code) {
        Compiler compiler = new Compiler(new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .emitVariableNames(true)
                //.setDebug()
                .build());
        CompilationResult result = compiler.compile(code);
        Assertions.assertNull(result.getDiagnostics());
        return result.getProgram();
    }

    public static class ApiRoot {
        public static Api api = new Api();
    }

    public static class Api {
        public String getNull() {
            return null;
        }
    }
}