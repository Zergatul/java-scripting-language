package com.zergatul.scripting.tests;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.lexer.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class LexerTest {

    @Test
    public void emptyInputTest() {
        LexerOutput result = lex("");
        Assertions.assertEquals(result.tokens().size(), 0);
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void lineNumbersTest1() {
        LexerOutput result = lex("(");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.LEFT_PARENTHESES, 1, 1, 0,1)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void lineNumbersTest2() {
        LexerOutput result = lex("\r()");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.WHITESPACE, 1, 1,0, 1),
                new Token(TokenType.LEFT_PARENTHESES, 2, 1, 1,1),
                new Token(TokenType.RIGHT_PARENTHESES, 2, 2, 2, 1)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void lineNumbersTest3() {
        LexerOutput result = lex("\r\r()");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.WHITESPACE, 1, 1, 0,2),
                new Token(TokenType.LEFT_PARENTHESES, 3, 1, 2, 1),
                new Token(TokenType.RIGHT_PARENTHESES, 3, 2, 3, 1)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void lineNumbersTest4() {
        LexerOutput result = lex("\r\n()");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.WHITESPACE, 1, 1, 0, 2),
                new Token(TokenType.LEFT_PARENTHESES, 2, 1, 2, 1),
                new Token(TokenType.RIGHT_PARENTHESES, 2, 2, 3, 1)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void lineNumbersTest5() {
        LexerOutput result = lex("\n\n()");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.WHITESPACE, 1, 1, 0, 2),
                new Token(TokenType.LEFT_PARENTHESES, 3, 1, 2, 1),
                new Token(TokenType.RIGHT_PARENTHESES, 3, 2, 3, 1)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void singleLineCommentTest1() {
        LexerOutput result = lex("//");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.WHITESPACE, 1, 1, 0, 2)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void singleLineCommentTest2() {
        LexerOutput result = lex("//abc");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.WHITESPACE, 1, 1, 0, 5)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void singleLineCommentTest3() {
        LexerOutput result = lex("//abc\r[");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.WHITESPACE, 1, 1, 0, 6),
                new Token(TokenType.LEFT_SQUARE_BRACKET, 2, 1, 6, 1)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }
    @Test
    public void singleLineCommentTest4() {
        LexerOutput result = lex("//abc\r\n[");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.WHITESPACE, 1, 1, 0, 7),
                new Token(TokenType.LEFT_SQUARE_BRACKET, 2, 1, 7, 1)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }
    @Test
    public void singleLineCommentTest5() {
        LexerOutput result = lex("//abc\n[");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.WHITESPACE, 1, 1, 0, 6),
                new Token(TokenType.LEFT_SQUARE_BRACKET, 2, 1, 6, 1)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void multiLineCommentTest1() {
        LexerOutput result = lex("/*");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.WHITESPACE, 1, 1, 0, 2)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void multiLineCommentTest2() {
        LexerOutput result = lex("/*/[");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.WHITESPACE, 1, 1, 0, 4)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }
    @Test
    public void multiLineCommentTest3() {
        LexerOutput result = lex("/**/[");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.WHITESPACE, 1, 1, 0, 4),
                new Token(TokenType.LEFT_SQUARE_BRACKET, 1, 5, 4, 1)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void multiLineCommentTest4() {
        LexerOutput result = lex("/*\n*/[");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.WHITESPACE, 1, 1, 0, 5),
                new Token(TokenType.LEFT_SQUARE_BRACKET, 2, 3, 5, 1)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void identifierTest1() {
        LexerOutput result = lex("a");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new IdentifierToken("a", 1, 1, 0, 1)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void identifierTest2() {
        LexerOutput result = lex("_QwertY091");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new IdentifierToken("_QwertY091", 1, 1, 0, 10)));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void invalidSymbolTest() {
        LexerOutput result = lex("\0");
        Assertions.assertEquals(result.tokens().size(), 0);
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.UnexpectedSymbol, new Token(TokenType.INVALID, 1, 1, 0, 1), "0000")));
    }

    @Test
    public void invocationTest() {
        LexerOutput result = lex("abc.qwe.x();");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new IdentifierToken("abc", 1, 1, 0, 3),
                new Token(TokenType.DOT, 1, 4, 3, 1),
                new IdentifierToken("qwe", 1, 5, 4, 3),
                new Token(TokenType.DOT, 1, 8, 7, 1),
                new IdentifierToken("x", 1, 9, 8, 1),
                new Token(TokenType.LEFT_PARENTHESES, 1, 10, 9, 1),
                new Token(TokenType.RIGHT_PARENTHESES, 1, 11, 10, 1),
                new Token(TokenType.SEMICOLON, 1, 12, 11, 1)
        ));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void assignmentOperatorsTest() {
        LexerOutput result = lex("+=-=*=/=");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.PLUS_EQUAL, 1, 1, 0, 2),
                new Token(TokenType.MINUS_EQUAL, 1, 3, 2, 2),
                new Token(TokenType.ASTERISK_EQUAL, 1, 5, 4, 2),
                new Token(TokenType.SLASH_EQUAL, 1, 7, 6, 2)
        ));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    private LexerOutput lex(String code) {
        return new Lexer(new LexerInput(code)).lex();
    }
}