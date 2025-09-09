package com.zergatul.scripting.tests.lexer;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BasicTests extends LexerTestBase {

    @Test
    public void emptyInputTest() {
        LexerOutput result = lex("");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                List.of(
                        new EndOfFileToken(new SingleLineTextRange(1, 1, 0,0))),
                result.tokens());
    }

    @Test
    public void lineNumbersTest1() {
        LexerOutput result = lex("(");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                List.of(
                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 1, 0,1)),
                        new EndOfFileToken(new SingleLineTextRange(1, 2, 1, 0))),
                result.tokens());
    }

    @Test
    public void lineNumbersTest2() {
        LexerOutput result = lex("\r()");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                List.of(
                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 1, 1, 1))
                                .withLeadingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 1, 2, 1, 0, 1))),
                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 2, 2, 1)),
                        new EndOfFileToken(new SingleLineTextRange(2, 3, 3, 0))),
                result.tokens());
    }

    @Test
    public void lineNumbersTest3() {
        LexerOutput result = lex("\r\r()");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                List.of(
                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(3, 1, 2, 1))
                                .withLeadingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 1, 2, 1, 0, 1)))
                                .withLeadingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(2, 1, 3, 1, 1, 1))),
                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(3, 2, 3, 1)),
                        new EndOfFileToken(new SingleLineTextRange(3, 3, 4, 0))),
                result.tokens());
    }

    @Test
    public void lineNumbersTest4() {
        LexerOutput result = lex("\r\n()");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                List.of(
                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 1, 2, 1))
                                .withLeadingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 1, 2, 1, 0, 2))),
                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 2, 3, 1)),
                        new EndOfFileToken(new SingleLineTextRange(2, 3, 4, 0))),
                result.tokens());
    }

    @Test
    public void lineNumbersTest5() {
        LexerOutput result = lex("\n\n()");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                List.of(
                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(3, 1, 2, 1))
                                .withLeadingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 1, 2, 1, 0, 1)))
                                .withLeadingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(2, 1, 3, 1, 1, 1))),
                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(3, 2, 3, 1)),
                        new EndOfFileToken(new SingleLineTextRange(3, 3, 4, 0))),
                result.tokens());
    }

    @Test
    public void invalidSymbolTest() {
        LexerOutput result = lex("\0");
        comparator.assertEquals(List.of(
                new DiagnosticMessage(LexerErrors.UnexpectedSymbol, new Token(TokenType.INVALID, new SingleLineTextRange(1, 1, 0, 1)), "0000")),
                result.diagnostics());
        comparator.assertEquals(
                List.of(
                        new Token(TokenType.INVALID, new SingleLineTextRange(1, 1, 0, 1)),
                        new EndOfFileToken(new SingleLineTextRange(1, 2, 1, 0))),
                result.tokens());
    }

    @Test
    public void invocationTest() {
        LexerOutput result = lex("abc.qwe.x();");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                List.of(
                        new ValueToken(TokenType.IDENTIFIER, "abc", new SingleLineTextRange(1, 1, 0, 3)),
                        new Token(TokenType.DOT, new SingleLineTextRange(1, 4, 3, 1)),
                        new ValueToken(TokenType.IDENTIFIER, "qwe", new SingleLineTextRange(1, 5, 4, 3)),
                        new Token(TokenType.DOT, new SingleLineTextRange(1, 8, 7, 1)),
                        new ValueToken(TokenType.IDENTIFIER, "x", new SingleLineTextRange(1, 9, 8, 1)),
                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 10, 9, 1)),
                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 11, 10, 1)),
                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 12, 11, 1)),
                        new EndOfFileToken(new SingleLineTextRange(1, 13, 12, 0))),
                result.tokens());
    }

    @Test
    public void assignmentOperatorsTest() {
        LexerOutput result = lex("+=-=*=/=");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                List.of(
                        new Token(TokenType.PLUS_EQUAL, new SingleLineTextRange(1, 1, 0, 2)),
                        new Token(TokenType.MINUS_EQUAL, new SingleLineTextRange(1, 3, 2, 2)),
                        new Token(TokenType.ASTERISK_EQUAL, new SingleLineTextRange(1, 5, 4, 2)),
                        new Token(TokenType.SLASH_EQUAL, new SingleLineTextRange(1, 7, 6, 2)),
                        new EndOfFileToken(new SingleLineTextRange(1, 9, 8, 0))),
                result.tokens());
    }
}