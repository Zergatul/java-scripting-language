package com.zergatul.scripting.tests.highlighting;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.highlighting.HighlightingProvider;
import com.zergatul.scripting.highlighting.SemanticToken;
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
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 3)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 5, 4, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(1, 6, 5, 1)),
                        new SemanticToken(SemanticTokenType.COMMENT, new SingleLineTextRange(1, 8, 7, 2)),
                        new SemanticToken(SemanticTokenType.COMMENT, new SingleLineTextRange(2, 1, 10, 5)),
                        new SemanticToken(SemanticTokenType.COMMENT, new SingleLineTextRange(3, 1, 16, 5)),
                        new SemanticToken(SemanticTokenType.COMMENT, new SingleLineTextRange(4, 1, 22, 5)),
                        new SemanticToken(SemanticTokenType.COMMENT, new SingleLineTextRange(5, 1, 28, 2)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(5, 4, 31, 3)),
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
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 4, 3, 3)),
                        new SemanticToken(SemanticTokenType.ARROW, new SingleLineTextRange(1, 8, 7, 2)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 11, 10, 3)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 14, 13, 1)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 16, 15, 4)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(1, 21, 20, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 23, 22, 1)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 24, 23, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 25, 24, 1)),
                        new SemanticToken(SemanticTokenType.ARROW, new SingleLineTextRange(1, 27, 26, 2))),
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