package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.*;
import com.zergatul.scripting.utility.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.zergatul.scripting.tests.completion.helpers.CommonSuggestions.*;

public class CustomInterfaceTests {

    @Test
    public void externalParametersTest1() {
        String code =
                "<cursor>\n";
        assertSuggestions(
                code,
                Interface1.class,
                context -> Lists.from(
                        unitMembers,
                        statements,
                        new KeywordSuggestion(TokenType.ASYNC),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new InputParameterSuggestion("value1", SInt.instance),
                        new InputParameterSuggestion("text", SString.instance)));
    }

    @Test
    public void externalParametersTest2() {
        String code =
                "int a = 3;\n" +
                "<cursor>\n";
        assertSuggestions(
                code,
                Interface1.class,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "a"),
                        new InputParameterSuggestion("value1", SInt.instance),
                        new InputParameterSuggestion("text", SString.instance)));
    }

    @Test
    public void externalParametersTest3() {
        String code =
                "int a = 3;\n" +
                "<cursor>\n" +
                "int c = 5;\n";
        assertSuggestions(
                code,
                Interface1.class,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "a"),
                        new InputParameterSuggestion("value1", SInt.instance),
                        new InputParameterSuggestion("text", SString.instance)));
    }

    @Test
    public void externalParametersTest4() {
        String code =
                "if (message.length > 0) {\n" +
                "    intStorage.add(<cursor>\n" +
                "}\n";
        assertSuggestions(
                code,
                Interface2.class,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new InputParameterSuggestion("message", SString.instance)));
    }

    @Test
    public void externalParametersTest5() {
        String code =
                "void func() {\n" +
                "    <cursor>\n" +
                "}\n" +
                "                \n" +
                "if (message.length > 0) {\n" +
                "    func();\n" +
                "}\n";
        assertSuggestions(
                code,
                Interface2.class,
                context -> Lists.from(
                        statements,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new FunctionSuggestion(context, "func")));
    }

    @Test
    public void externalParametersTest7() {
        String code =
                "int x = a<cursor>\n";
        assertSuggestions(
                code,
                Interface2.class,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new InputParameterSuggestion("message", SString.instance)));
    }

    @Test
    public void externalParametersTest6() {
        String code =
                "static int x = a<cursor>\n" +
                "                \n" +
                "if (message.length > 0) {\n" +
                "    func();\n" +
                "}\n";
        assertSuggestions(
                code,
                Interface2.class,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new StaticFieldSuggestion(context, "x")));
    }

    @Test
    public void externalParameterTest7() {
        String code =
                "return a<cursor>\n";
        assertSuggestions(
                code,
                Interface3.class,
                context -> Lists.from(
                        expressions,
                        new StaticConstantSuggestion(context, "intStorage"),
                        new InputParameterSuggestion("input", SInt.instance)));
    }

    private void assertSuggestions(String code, Class<?> functionalInterface, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        CompletionTestHelper.assertSuggestions(ApiRoot.class, code, functionalInterface, expectedFactory);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }

    @FunctionalInterface
    public interface Interface1 {
        void invoke(int value1, String text);
    }

    @FunctionalInterface
    public interface Interface2 {
        void run(String message);
    }

    @FunctionalInterface
    public interface Interface3 {
        int run(int input);
    }
}