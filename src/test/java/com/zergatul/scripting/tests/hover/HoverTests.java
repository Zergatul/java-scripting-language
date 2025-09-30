package com.zergatul.scripting.tests.hover;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.highlighting.SemanticTokenType;
import com.zergatul.scripting.hover.HoverProvider;
import com.zergatul.scripting.hover.Theme;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class HoverTests {

    private static final String TOKEN_COLOR = "111";
    private static final String PRED_TYPE_COLOR = "222";
    private static final String TYPE_COLOR = "333";
    private static final String METHOD_COLOR = "444";
    private static final String DESCRIPTION_COLOR = "555";
    private static final String PARAMETER_COLOR = "666";

    @Test
    public void booleanLiteralTest() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = true;
                """, 1, 9);
        Assertions.assertEquals(hover, new HoverProvider.HoverResponse(List.of(
                String.format("<span style=\"color:#%s;\">boolean</span>", PRED_TYPE_COLOR),
                String.format("<span style=\"color:#%s;\">true or false value</span>", DESCRIPTION_COLOR)
        ), new SingleLineTextRange(1, 9, 8, 4)));
    }

    @Test
    public void charLiteralTest() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = 'a';
                """, 1, 9);
        Assertions.assertEquals(hover, new HoverProvider.HoverResponse(List.of(
                String.format("<span style=\"color:#%s;\">char</span>", PRED_TYPE_COLOR),
                String.format("<span style=\"color:#%s;\">Single character</span>", DESCRIPTION_COLOR)
        ), new SingleLineTextRange(1, 9, 8, 3)));
    }

    @Test
    public void int32LiteralTest() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = 1234;
                """, 1, 9);
        Assertions.assertEquals(hover, new HoverProvider.HoverResponse(List.of(
                String.format("<span style=\"color:#%s;\">int</span>", PRED_TYPE_COLOR),
                String.format("<span style=\"color:#%s;\">32-bit signed integer</span>", DESCRIPTION_COLOR)
        ), new SingleLineTextRange(1, 9, 8, 4)));
    }

    @Test
    public void int64LiteralTest() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = 123L;
                """, 1, 9);
        Assertions.assertEquals(hover, new HoverProvider.HoverResponse(List.of(
                String.format("<span style=\"color:#%s;\">long</span>", PRED_TYPE_COLOR),
                String.format("<span style=\"color:#%s;\">64-bit signed integer</span>", DESCRIPTION_COLOR)
        ), new SingleLineTextRange(1, 9, 8, 4)));
    }

    @Test
    public void floatLiteralTest() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = 0.03;
                """, 1, 9);
        Assertions.assertEquals(hover, new HoverProvider.HoverResponse(List.of(
                String.format("<span style=\"color:#%s;\">float</span>", PRED_TYPE_COLOR),
                String.format("<span style=\"color:#%s;\">Double-precision floating-point number</span>", DESCRIPTION_COLOR)
        ), new SingleLineTextRange(1, 9, 8, 4)));
    }

    @Test
    public void stringLiteralTest() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = "aa";
                """, 1, 9);
        Assertions.assertEquals(hover, new HoverProvider.HoverResponse(List.of(
                String.format("<span style=\"color:#%s;\">string</span>", PRED_TYPE_COLOR),
                String.format("<span style=\"color:#%s;\">Text as sequence of characters</span>", DESCRIPTION_COLOR)
        ), new SingleLineTextRange(1, 9, 8, 4)));
    }

    @Test
    public void localVariableTest() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = "aa";
                """, 1, 5);
        Assertions.assertEquals(hover, new HoverProvider.HoverResponse(List.of(
                String.format("<span style=\"color:#%s;\">(local variable)</span> <span style=\"color:#%s;\">string</span> <span style=\"color:#%s;\">x</span>",
                        DESCRIPTION_COLOR, PRED_TYPE_COLOR, DESCRIPTION_COLOR)
        ), new SingleLineTextRange(1, 5, 4, 1)));
    }

    @Test
    public void parameterTest() {
        HoverProvider.HoverResponse hover = getHover("""
                void func(int a) {
                    a
                }
                """, 2, 5);
        Assertions.assertEquals(hover, new HoverProvider.HoverResponse(List.of(
                String.format("<span style=\"color:#%s;\">(parameter)</span> <span style=\"color:#%s;\">int</span> <span style=\"color:#%s;\">a</span>",
                        DESCRIPTION_COLOR, PRED_TYPE_COLOR, DESCRIPTION_COLOR)
        ), new SingleLineTextRange(2, 5, 23, 1)));
    }

    @Test
    public void externalStaticConstantTest() {
        HoverProvider.HoverResponse hover = getHover("""
                intStorage
                """, 1, 1);
        Assertions.assertEquals(hover, new HoverProvider.HoverResponse(List.of(
                String.format("<span style=\"color:#%s;\">(external static constant)</span> <span style=\"color:#%s;\">com.zergatul.scripting.tests.compiler.helpers.IntStorage</span> <span style=\"color:#%s;\">intStorage</span>",
                        DESCRIPTION_COLOR, TYPE_COLOR, DESCRIPTION_COLOR)
        ), new SingleLineTextRange(1, 1, 0, 10)));
    }

    @Test
    public void binaryOperationTest1() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = 1 + 2;
                """, 1, 11);
        Assertions.assertEquals(hover, new HoverProvider.HoverResponse(List.of(
                String.format("<span style=\"color:#%s;\">int</span> <span style=\"color:#%s;\">+</span><span style=\"color:#%s;\">(</span><span style=\"color:#%s;\">int</span> <span style=\"color:#%s;\">left</span><span style=\"color:#%s;\">,</span> <span style=\"color:#%s;\">int</span> <span style=\"color:#666;\">right</span><span style=\"color:#555;\">)</span>",
                        PRED_TYPE_COLOR, DESCRIPTION_COLOR, DESCRIPTION_COLOR, PRED_TYPE_COLOR, PARAMETER_COLOR, DESCRIPTION_COLOR, PRED_TYPE_COLOR, PARAMETER_COLOR, DESCRIPTION_COLOR)
        ), new SingleLineTextRange(1, 11, 10, 1)));
    }

    @Test
    public void binaryOperationTest2() {
        HoverProvider.HoverResponse hover = getHover("""
                let x = [1, 2, 3] + 4;
                """, 1, 19);
        Assertions.assertEquals(hover, new HoverProvider.HoverResponse(List.of(
                String.format("<span style=\"color:#222;\">int</span><span style=\"color:#111;\">[]</span> <span style=\"color:#555;\">+</span><span style=\"color:#555;\">(</span><span style=\"color:#222;\">int</span><span style=\"color:#111;\">[]</span> <span style=\"color:#666;\">left</span><span style=\"color:#555;\">,</span> <span style=\"color:#222;\">int</span> <span style=\"color:#666;\">right</span><span style=\"color:#555;\">)</span>")
        ), new SingleLineTextRange(1, 19, 18, 1)));
    }

    @Test
    public void classTest() {
        HoverProvider.HoverResponse hover = getHover("""
                class MyType {}
                MyType x;
                """, 2, 8);
        Assertions.assertEquals(hover, new HoverProvider.HoverResponse(List.of(
                String.format("<span style=\"color:#555;\">(local variable)</span> <span style=\"color:#333;\">MyType</span> <span style=\"color:#555;\">x</span>")
        ), new SingleLineTextRange(2, 8, 23, 1)));
    }

    private static HoverProvider.HoverResponse getHover(String code, int line, int column) {
        return getHover(code, ApiRoot.class, Runnable.class, line, column);
    }

    private static HoverProvider.HoverResponse getHover(String code, Class<?> root, Class<?> functionalInterface, int line, int column) {
        CompilationParameters parameters = new CompilationParametersBuilder()
                .setRoot(root)
                .setInterface(functionalInterface)
                .build();

        LexerInput lexerInput = new LexerInput(code);
        Lexer lexer = new Lexer(lexerInput);
        LexerOutput lexerOutput = lexer.lex();

        Parser parser = new Parser(lexerOutput);
        ParserOutput parserOutput = parser.parse();

        Binder binder = new Binder(parserOutput, parameters);
        BinderOutput binderOutput = binder.bind();

        HoverProvider provider = new HoverProvider(new TestTheme());
        return provider.get(binderOutput, line, column);
    }

    private static class ApiRoot {
        public static IntStorage intStorage;
    }

    private static class TestTheme extends Theme {

        @Override
        public String getTokenColor(SemanticTokenType type) {
            return TOKEN_COLOR;
        }

        @Override
        public String getPredefinedTypeColor() {
            return PRED_TYPE_COLOR;
        }

        @Override
        public String getTypeColor() {
            return TYPE_COLOR;
        }

        @Override
        public String getMethodColor() {
            return METHOD_COLOR;
        }

        @Override
        public String getDescriptionColor() {
            return DESCRIPTION_COLOR;
        }

        @Override
        public String getParameterColor() {
            return PARAMETER_COLOR;
        }
    }
}