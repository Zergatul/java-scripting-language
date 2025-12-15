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

public class IsExpressionTests extends ComparatorTest {

    @Test
    public void constantPatternTest() {
        String code = """
                int x;
                if (x is "100") {}
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(1, 1, 0, 3)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 5, 4, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(1, 6, 5, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(2, 1, 7, 2)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 4, 10, 1)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(2, 5, 11, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.OPERATOR_LIKE), new SingleLineTextRange(2, 7, 13, 2)),
                        new SemanticToken(SemanticTokenType.STRING, new SingleLineTextRange(2, 10, 16, 5)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 15, 21, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 17, 23, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 18, 24, 1))),
                highlight(code));
    }

    @Test
    public void notPatternTest() {
        String code = """
                string str;
                if (str is not null) {}
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(1, 1, 0, 6)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 8, 7, 3)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(1, 11, 10, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(2, 1, 12, 2)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 4, 15, 1)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(2, 5, 16, 3)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.OPERATOR_LIKE), new SingleLineTextRange(2, 9, 20, 2)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(2, 12, 23, 3)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(2, 16, 27, 4)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 20, 31, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 22, 33, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 23, 34, 1))),
                highlight(code));
    }

    @Test
    public void declarationPatternTest() {
        String code = """
                string o;
                if (o is not string str) {}
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(1, 1, 0, 6)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(1, 8, 7, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(1, 9, 8, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(2, 1, 10, 2)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 4, 13, 1)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(2, 5, 14, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.OPERATOR_LIKE), new SingleLineTextRange(2, 7, 16, 2)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(2, 10, 19, 3)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(2, 14, 23, 6)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(2, 21, 30, 3)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 24, 33, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 26, 35, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 27, 36, 1))),
                highlight(code));
    }

    private List<SemanticToken> highlight(String code) {
        LexerOutput lexerOutput = new Lexer(new LexerInput(code)).lex();
        ParserOutput parserOutput = new Parser(lexerOutput).parse();
        BinderOutput binderOutput = new Binder(parserOutput, new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .build()).bind();
        return new HighlightingProvider(lexerOutput, binderOutput).get();
    }

    public static class ApiRoot {}
}