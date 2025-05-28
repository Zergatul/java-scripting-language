package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.helpers.CompletionTestHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

public class CustomInterfaceTests {

    @Test
    public void externalParametersTest1() {
        assertSuggestions("""
                <cursor>
                """,
                Interface1.class,
                context -> List.of(
                        new KeywordSuggestion(TokenType.STATIC),
                        new KeywordSuggestion(TokenType.VOID),
                        new KeywordSuggestion(TokenType.LET),
                        new KeywordSuggestion(TokenType.FOR),
                        new KeywordSuggestion(TokenType.FOREACH),
                        new KeywordSuggestion(TokenType.IF),
                        new KeywordSuggestion(TokenType.WHILE),
                        new KeywordSuggestion(TokenType.RETURN),
                        new TypeSuggestion(SBoolean.instance),
                        new TypeSuggestion(SInt.instance),
                        new TypeSuggestion(SInt64.instance),
                        new TypeSuggestion(SChar.instance),
                        new TypeSuggestion(SFloat.instance),
                        new TypeSuggestion(SString.instance),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new InputParameterSuggestion("value1", SInt.instance),
                        new InputParameterSuggestion("text", SString.instance)));
    }

    @Test
    public void externalParametersTest2() {
        assertSuggestions("""
                int a = 3;
                <cursor>
                """,
                Interface1.class,
                context -> List.of(
                        new KeywordSuggestion(TokenType.LET),
                        new KeywordSuggestion(TokenType.FOR),
                        new KeywordSuggestion(TokenType.FOREACH),
                        new KeywordSuggestion(TokenType.IF),
                        new KeywordSuggestion(TokenType.WHILE),
                        new KeywordSuggestion(TokenType.RETURN),
                        new TypeSuggestion(SBoolean.instance),
                        new TypeSuggestion(SInt.instance),
                        new TypeSuggestion(SInt64.instance),
                        new TypeSuggestion(SChar.instance),
                        new TypeSuggestion(SFloat.instance),
                        new TypeSuggestion(SString.instance),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "a"),
                        new InputParameterSuggestion("value1", SInt.instance),
                        new InputParameterSuggestion("text", SString.instance)));
    }

    @Test
    public void externalParametersTest3() {
        assertSuggestions("""
                int a = 3;
                <cursor>
                int c = 5;
                """,
                Interface1.class,
                context -> List.of(
                        new KeywordSuggestion(TokenType.LET),
                        new KeywordSuggestion(TokenType.FOR),
                        new KeywordSuggestion(TokenType.FOREACH),
                        new KeywordSuggestion(TokenType.IF),
                        new KeywordSuggestion(TokenType.WHILE),
                        new KeywordSuggestion(TokenType.RETURN),
                        new TypeSuggestion(SBoolean.instance),
                        new TypeSuggestion(SInt.instance),
                        new TypeSuggestion(SInt64.instance),
                        new TypeSuggestion(SChar.instance),
                        new TypeSuggestion(SFloat.instance),
                        new TypeSuggestion(SString.instance),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new LocalVariableSuggestion(context, "a"),
                        new InputParameterSuggestion("value1", SInt.instance),
                        new InputParameterSuggestion("text", SString.instance)));
    }

    @Test
    public void externalParametersTest4() {
        assertSuggestions("""
                if (message.length > 0) {
                    intStorage.add(<cursor>
                }
                """,
                Interface2.class,
                context -> List.of(
                        new TypeSuggestion(SBoolean.instance),
                        new TypeSuggestion(SInt.instance),
                        new TypeSuggestion(SInt64.instance),
                        new TypeSuggestion(SChar.instance),
                        new TypeSuggestion(SFloat.instance),
                        new TypeSuggestion(SString.instance),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new InputParameterSuggestion("message", SString.instance)));
    }

    @Test
    public void externalParametersTest5() {
        assertSuggestions("""
                void func() {
                    <cursor>
                }
                
                if (message.length > 0) {
                    func();
                }
                """,
                Interface2.class,
                context -> List.of(
                        new KeywordSuggestion(TokenType.LET),
                        new KeywordSuggestion(TokenType.FOR),
                        new KeywordSuggestion(TokenType.FOREACH),
                        new KeywordSuggestion(TokenType.IF),
                        new KeywordSuggestion(TokenType.WHILE),
                        new KeywordSuggestion(TokenType.RETURN),
                        new TypeSuggestion(SBoolean.instance),
                        new TypeSuggestion(SInt.instance),
                        new TypeSuggestion(SInt64.instance),
                        new TypeSuggestion(SChar.instance),
                        new TypeSuggestion(SFloat.instance),
                        new TypeSuggestion(SString.instance),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new FunctionSuggestion(context, "func")));
    }

    @Test
    public void externalParametersTest7() {
        assertSuggestions("""
                int x = a<cursor>
                """,
                Interface2.class,
                context -> List.of(
                        new TypeSuggestion(SBoolean.instance),
                        new TypeSuggestion(SInt.instance),
                        new TypeSuggestion(SInt64.instance),
                        new TypeSuggestion(SChar.instance),
                        new TypeSuggestion(SFloat.instance),
                        new TypeSuggestion(SString.instance),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new InputParameterSuggestion("message", SString.instance)));
    }

    @Test
    public void externalParametersTest6() {
        assertSuggestions("""
                static int x = a<cursor>
                
                if (message.length > 0) {
                    func();
                }
                """,
                Interface2.class,
                context -> List.of(
                        new TypeSuggestion(SBoolean.instance),
                        new TypeSuggestion(SInt.instance),
                        new TypeSuggestion(SInt64.instance),
                        new TypeSuggestion(SChar.instance),
                        new TypeSuggestion(SFloat.instance),
                        new TypeSuggestion(SString.instance),
                        new StaticConstantSuggestion(context, "intStorage"),
                        new StaticFieldSuggestion(context, "x")));
    }

    @Test
    public void externalParameterTest7() {
        assertSuggestions("""
                return a<cursor>
                """,
                Interface3.class,
                context -> List.of(
                        new TypeSuggestion(SBoolean.instance),
                        new TypeSuggestion(SInt.instance),
                        new TypeSuggestion(SInt64.instance),
                        new TypeSuggestion(SChar.instance),
                        new TypeSuggestion(SFloat.instance),
                        new TypeSuggestion(SString.instance),
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