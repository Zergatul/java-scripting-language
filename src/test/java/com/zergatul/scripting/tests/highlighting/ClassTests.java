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

public class ClassTests extends ComparatorTest {

    @Test
    public void binaryOperatorTest() {
        String code = """
                class MyClass {
                    operator [+] MyClass(MyClass left, MyClass right) {
                        return new MyClass();
                    }
                }
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 5)),
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(1, 7, 6, 7)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 15, 14, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(2, 5, 20, 8)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 14, 29, 1)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(2, 15, 30, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 16, 31, 1)),
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(2, 18, 33, 7)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 25, 40, 1)),
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(2, 26, 41, 7)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(2, 34, 49, 4)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(2, 38, 53, 1)),
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(2, 40, 55, 7)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(2, 48, 63, 5)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 53, 68, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 55, 70, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(3, 9, 80, 6)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.OPERATOR_LIKE), new SingleLineTextRange(3, 16, 87, 3)),
                        new SemanticToken(SemanticTokenType.TYPE, new SingleLineTextRange(3, 20, 91, 7)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 27, 98, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 28, 99, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(3, 29, 100, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(4, 5, 106, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(5, 1, 108, 1))),
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