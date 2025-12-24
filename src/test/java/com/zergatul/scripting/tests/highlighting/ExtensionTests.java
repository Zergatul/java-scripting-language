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

public class ExtensionTests extends ComparatorTest {

    @Test
    public void basicTest() {
        String code = """
                extension(string) {
                    operator [+] int(string str) => 0;
                    operator [/] string[](string str, char ch) => str.split(ch);
                    int len() => this.length;
                }
                """;
        comparator.assertSemanticEquals(
                List.of(
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(1, 1, 0, 9)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 10, 9, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(1, 11, 10, 6)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 17, 16, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(1, 19, 18, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(2, 5, 24, 8)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 14, 33, 1)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(2, 15, 34, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 16, 35, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(2, 18, 37, 3)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 21, 40, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(2, 22, 41, 6)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(2, 29, 48, 3)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(2, 32, 51, 1)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(2, 34, 53, 2)),
                        new SemanticToken(SemanticTokenType.NUMBER, new SingleLineTextRange(2, 37, 56, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(2, 38, 57, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(3, 5, 63, 8)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 14, 72, 1)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(3, 15, 73, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 16, 74, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(3, 18, 76, 6)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 24, 82, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 25, 83, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 26, 84, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(3, 27, 85, 6)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(3, 34, 92, 3)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(3, 37, 95, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(3, 39, 97, 4)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(3, 44, 102, 2)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 46, 104, 1)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(3, 48, 106, 2)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(3, 51, 109, 3)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(3, 54, 112, 1)),
                        new SemanticToken(SemanticTokenType.METHOD, new SingleLineTextRange(3, 55, 113, 5)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 60, 118, 1)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(3, 61, 119, 2)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(3, 63, 121, 1)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(3, 64, 122, 1)),
                        new SemanticToken(SemanticTokenType.KEYWORD, List.of(SemanticTokenModifier.PREDEFINED_TYPE), new SingleLineTextRange(4, 5, 128, 3)),
                        new SemanticToken(SemanticTokenType.IDENTIFIER, new SingleLineTextRange(4, 9, 132, 3)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(4, 12, 135, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(4, 13, 136, 1)),
                        new SemanticToken(SemanticTokenType.OPERATOR, new SingleLineTextRange(4, 15, 138, 2)),
                        new SemanticToken(SemanticTokenType.KEYWORD, new SingleLineTextRange(4, 18, 141, 4)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(4, 22, 145, 1)),
                        new SemanticToken(SemanticTokenType.PROPERTY, new SingleLineTextRange(4, 23, 146, 6)),
                        new SemanticToken(SemanticTokenType.SEPARATOR, new SingleLineTextRange(4, 29, 152, 1)),
                        new SemanticToken(SemanticTokenType.BRACKET, new SingleLineTextRange(5, 1, 154, 1))),
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
