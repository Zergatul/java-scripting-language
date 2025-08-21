package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VariableNameTests {

    @Test
    public void unitVariableTest() {
        Runnable program = compile("""
                let str = api.getNull();
                str = str.substring(10);
                """);

        try {
            program.run();
        } catch (NullPointerException exception) {
            Assertions.assertEquals(exception.getMessage(), "Cannot invoke \"String.substring(int)\" because \"str\" is null");
            return;
        }

        Assertions.fail();
    }

    @Test
    public void functionVariableTest() {
        Runnable program = compile("""
                void func() {
                    string x = api.getNull();
                    x.substring(10);
                }
                
                func();
                """);

        try {
            program.run();
        } catch (NullPointerException exception) {
            Assertions.assertEquals(exception.getMessage(), "Cannot invoke \"String.substring(int)\" because \"x\" is null");
            return;
        }

        Assertions.fail();
    }

    @Test
    public void functionParameterTest() {
        Runnable program = compile("""
                void func(string abc) {
                    abc.substring(10);
                }
                
                func(api.getNull());
                """);

        try {
            program.run();
        } catch (NullPointerException exception) {
            Assertions.assertEquals(exception.getMessage(), "Cannot invoke \"String.substring(int)\" because \"abc\" is null");
            return;
        }

        Assertions.fail();
    }

    @Test
    public void variableOverlayTest1() {
        Runnable program = compile("""
                string getString(boolean isNull) => isNull ? api.getNull() : "";
                
                {
                    string str1 = getString(true);
                    str1.substring(0);
                }
                {
                    string str2 = getString(false);
                    str2.substring(0);
                }
                """);

        try {
            program.run();
        } catch (NullPointerException exception) {
            Assertions.assertEquals(exception.getMessage(), "Cannot invoke \"String.substring(int)\" because \"str1\" is null");
            return;
        }

        Assertions.fail();
    }

    @Test
    public void variableOverlayTest2() {
        Runnable program = compile("""
                string getString(boolean isNull) => isNull ? api.getNull() : "";
                
                {
                    string str1 = getString(false);
                    str1.substring(0);
                }
                {
                    string str2 = getString(true);
                    str2.substring(0);
                }
                """);

        try {
            program.run();
        } catch (NullPointerException exception) {
            Assertions.assertEquals(exception.getMessage(), "Cannot invoke \"String.substring(int)\" because \"str2\" is null");
            return;
        }

        Assertions.fail();
    }

    @Test
    public void forLoopTest() {
        Runnable program = compile("""
                let array = [api.getNull()];
                for (let i = 0; i < array.length; i++) {
                    let element = array[i];
                    element.substring(0);
                }
                """);

        try {
            program.run();
        } catch (NullPointerException exception) {
            Assertions.assertEquals(exception.getMessage(), "Cannot invoke \"String.substring(int)\" because \"element\" is null");
            return;
        }

        Assertions.fail();
    }

    @Test
    public void forEachLoopTest() {
        Runnable program = compile("""
                let array = [api.getNull()];
                foreach (let element in array) {
                    element.substring(0);
                }
                """);

        try {
            program.run();
        } catch (NullPointerException exception) {
            Assertions.assertEquals(exception.getMessage(), "Cannot invoke \"String.substring(int)\" because \"element\" is null");
            return;
        }

        Assertions.fail();
    }

    @Test
    public void whileLoopTest() {
        Runnable program = compile("""
                let myStr = api.getNull();
                while (true) {
                    myStr.substring(0);
                }
                """);

        try {
            program.run();
        } catch (NullPointerException exception) {
            Assertions.assertEquals(exception.getMessage(), "Cannot invoke \"String.substring(int)\" because \"myStr\" is null");
            return;
        }

        Assertions.fail();
    }

    @Test
    public void lambdaTest() {
        Runnable program = compile("""
                void process(fn<string => void> func) => func(api.getNull());
                
                process(sss => sss.substring(10));
                """);

        try {
            program.run();
        } catch (NullPointerException exception) {
            Assertions.assertEquals(exception.getMessage(), "Cannot invoke \"String.substring(int)\" because \"sss\" is null");
            return;
        }

        Assertions.fail();
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