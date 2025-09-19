package com.zergatul.scripting.tests.lexer;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BasicTests {

    @Test
    public void emptyInputTest() {
        LexerOutput result = lex("");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new EndOfFileToken(new SingleLineTextRange(1, 1, 0,0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void lineNumbersTest1() {
        LexerOutput result = lex("(");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 1, 0,1)),
                new EndOfFileToken(new SingleLineTextRange(1, 2, 1, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void lineNumbersTest2() {
        LexerOutput result = lex("\r()");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 1, 1,1))
                        .withLeadingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(1, 1, 0, 1))),
                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 2, 2, 1)),
                new EndOfFileToken(new SingleLineTextRange(2, 3, 3, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void lineNumbersTest3() {
        LexerOutput result = lex("\r\r()");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(3, 1, 2, 1))
                        .withLeadingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(1, 1,0, 1)))
                        .withLeadingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(2, 1,1, 1))),
                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(3, 2, 3, 1)),
                new EndOfFileToken(new SingleLineTextRange(3, 3, 4, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void lineNumbersTest4() {
        LexerOutput result = lex("\r\n()");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 1, 2, 1))
                        .withLeadingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(1, 1,0, 2))),
                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 2, 3, 1)),
                new EndOfFileToken(new SingleLineTextRange(2, 3, 4, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void lineNumbersTest5() {
        LexerOutput result = lex("\n\n()");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(3, 1, 2, 1))
                        .withLeadingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(1, 1,0, 1)))
                        .withLeadingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(2, 1,1, 1))),
                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(3, 2, 3, 1)),
                new EndOfFileToken(new SingleLineTextRange(3, 3, 4, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    private LexerOutput lex(String code) {
        return new Lexer(new LexerInput(code)).lex();
    }
}
