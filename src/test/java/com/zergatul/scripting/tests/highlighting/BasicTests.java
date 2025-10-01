package com.zergatul.scripting.tests.highlighting;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.highlighting.HighlightingProvider;
import com.zergatul.scripting.highlighting.SemanticToken;
import com.zergatul.scripting.highlighting.SemanticTokenModifier;
import com.zergatul.scripting.highlighting.SemanticTokenType;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BasicTests extends ComparatorTest {

    @Test
    public void multilineCommentTest1() {
        String code = """
                /* line1
                line2
                line3 */
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.COMMENT, new SingleLineTextRange(1, 1, 0, 8)),
                        new SemanticToken(SemanticTokenType.COMMENT, new SingleLineTextRange(2, 1, 9, 5)),
                        new SemanticToken(SemanticTokenType.COMMENT, new SingleLineTextRange(3, 1, 15, 8))),
                highlight(code));
    }

    @Test
    public void multilineCommentTest2() {
        String code = """
                int a; /*
                line1
                line2
                line3
                */ int b;
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(1, 1, 0, 3)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 5, 4, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(1, 6, 5, 1)),
                        new SemanticToken(SemanticTokenType.COMMENT, new SingleLineTextRange(1, 8, 7, 2)),
                        new SemanticToken(SemanticTokenType.COMMENT, new SingleLineTextRange(2, 1, 10, 5)),
                        new SemanticToken(SemanticTokenType.COMMENT, new SingleLineTextRange(3, 1, 16, 5)),
                        new SemanticToken(SemanticTokenType.COMMENT, new SingleLineTextRange(4, 1, 22, 5)),
                        new SemanticToken(SemanticTokenType.COMMENT, new SingleLineTextRange(5, 1, 28, 2)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(5, 4, 31, 3)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(5, 8, 35, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(5, 9, 36, 1))),
                highlight(code));
    }

    @Test
    public void unknownTypeTest() {
        String code = """
                MyType x;
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(1, 1, 0, 6)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 8, 7, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(1, 9, 8, 1))),
                highlight(code));
    }

    @Test
    public void genericFunctionTest1() {
        String code = """
                fn<int => int> func = (x) =>
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 2)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 3, 2, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(1, 4, 3, 3)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(1, 8, 7, 2)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(1, 11, 10, 3)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 14, 13, 1)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 16, 15, 4)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(1, 21, 20, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 23, 22, 1)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 24, 23, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 25, 24, 1)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(1, 27, 26, 2))),
                highlight(code));
    }

    @Test
    public void genericFunctionTest2() {
        String code = """
                fn<int => int> func = (x) => x
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 2)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 3, 2, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(1, 4, 3, 3)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(1, 8, 7, 2)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(1, 11, 10, 3)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 14, 13, 1)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 16, 15, 4)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(1, 21, 20, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 23, 22, 1)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 24, 23, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 25, 24, 1)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(1, 27, 26, 2)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 30, 29, 1))),
                highlight(code));
    }

    @Test
    public void classTest1() {
        String code = """
                class Region {
                    constructor() {
                        set
                    }
                    void set() {}
                }
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 5)),
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(1, 7, 6, 6)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 14, 13, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(2, 5, 19, 11)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 16, 30, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 17, 31, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 19, 33, 1)),
                        new SemanticToken(SemanticTokenType.METHOD, new SingleLineTextRange(3, 9, 43, 3)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(4, 5, 51, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(5, 5, 57, 4)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(5, 10, 62, 3)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(5, 13, 65, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(5, 14, 66, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(5, 16, 68, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(5, 17, 69, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(6, 1, 71, 1))),
                highlight(code));
    }

    @Test
    public void classTest2() {
        String code = """
                class Region {
                    int x;
                    void method() {
                        (x).toString();
                    }
                }
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 5)),
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(1, 7, 6, 6)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 14, 13, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(2, 5, 19, 3)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(2, 9, 23, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(2, 10, 24, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(3, 5, 30, 4)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(3, 10, 35, 6)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 16, 41, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 17, 42, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 19, 44, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(4, 9, 54, 1)),
                        new SemanticToken(SemanticTokenType.PROPERTY, new SingleLineTextRange(4, 10, 55, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(4, 11, 56, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(4, 12, 57, 1)),
                        new SemanticToken(SemanticTokenType.METHOD, new SingleLineTextRange(4, 13, 58, 8)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(4, 21, 66, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(4, 22, 67, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(4, 23, 68, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(5, 5, 74, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(6, 1, 76, 1))),
                highlight(code));
    }

    @Test
    public void newExpressionTest1() {
        String code = """
                let x = new
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 3)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 5, 4, 1)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(1, 7, 6, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.OPERATOR_LIKE), new SingleLineTextRange(1, 9, 8, 3))),
                highlight(code));
    }

    @Test
    public void newExpressionTest2() {
        String code = """
                let x = new X
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 3)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 5, 4, 1)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(1, 7, 6, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.OPERATOR_LIKE), new SingleLineTextRange(1, 9, 8, 3)),
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(1, 13, 12, 1))),
                highlight(code));
    }

    @Test
    public void whileLoopTest() {
        String code = """
                while (true) ;
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 5)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 7, 6, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.VALUE), new SingleLineTextRange(1, 8, 7, 4)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 12, 11, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(1, 14, 13, 1))),
                highlight(code));
    }

    @Test
    public void externalTest() {
        String code = """
                api.test();
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.IDENTIFIER, List.of(SemanticTokenModifier.EXTERNAL, SemanticTokenModifier.STATIC), new SingleLineTextRange(1, 1, 0, 3)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(1, 4, 3, 1)),
                        new SemanticToken(SemanticTokenType.METHOD, new SingleLineTextRange(1, 5, 4, 4)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 9, 8, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 10, 9, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(1, 11, 10, 1))),
                highlight(code));
    }

    @Test
    public void staticVariableTest() {
        String code = """
                static int x;
                x = 0;
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 6)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(1, 8, 7, 3)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, List.of(SemanticTokenModifier.STATIC), new SingleLineTextRange(1, 12, 11, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(1, 13, 12, 1)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, List.of(SemanticTokenModifier.STATIC), new SingleLineTextRange(2, 1, 14, 1)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(2, 3, 16, 1)),
                        new SemanticToken(SemanticTokenType.NUMBER, new SingleLineTextRange(2, 5, 18, 1))),
                highlight(code));
    }

    @Test
    public void functionTest() {
        String code = """
                void func(){}
                func();
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(1, 1, 0, 4)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, List.of(SemanticTokenModifier.FUNCTION), new SingleLineTextRange(1, 6, 5, 4)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 10, 9, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 11, 10, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 12, 11, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 13, 12, 1)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, List.of(SemanticTokenModifier.FUNCTION), new SingleLineTextRange(2, 1, 14, 4)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 5, 18, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 6, 19, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(2, 7, 20, 1))),
                highlight(code));
    }

    @Test
    public void unfinishedStaticVariableTest1() {
        String code = """
                static in
                api.test();
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 6)),
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(1, 8, 7, 2)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, List.of(SemanticTokenModifier.STATIC), new SingleLineTextRange(2, 1, 10, 3)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(2, 5, 14, 4)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 9, 18, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 10, 19, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(2, 11, 20, 1))),
                highlight(code));
    }

    @Test
    public void unfinishedStaticVariableTest2() {
        String code = """
                static int x =
                boolean show = true;
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 6)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(1, 8, 7, 3)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, List.of(SemanticTokenModifier.STATIC), new SingleLineTextRange(1, 12, 11, 1)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(1, 14, 13, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(2, 1, 15, 7)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(2, 9, 23, 4)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(2, 14, 28, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.VALUE), new SingleLineTextRange(2, 16, 30, 4))),
                highlight(code));
    }

    @Test
    public void unfinishedFunctionTest() {
        String code = """
                void
                api.test();
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(1, 1, 0, 4)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, List.of(SemanticTokenModifier.FUNCTION), new SingleLineTextRange(2, 1, 5, 3)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(2, 5, 9, 4)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 9, 13, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 10, 14, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(2, 11, 15, 1))),
                highlight(code));
    }

    protected List<SemanticToken> highlight(String code) {
        LexerOutput lexerOutput = new Lexer(new LexerInput(code)).lex();
        ParserOutput parserOutput = new Parser(lexerOutput).parse();
        BinderOutput binderOutput = new Binder(parserOutput, new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .build()).bind();
        return new HighlightingProvider(lexerOutput, binderOutput).get();
    }

    public static class ApiRoot {
        public static Api api = new Api();
    }

    public static class Api {
        public void test() {}
    }
}