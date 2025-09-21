package com.zergatul.scripting.tests.lexer;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CharTests extends LexerTestBase {

    @Test
    public void charEmptyTest() {
        LexerOutput result = lex("''");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, String.valueOf((char) 0), new SingleLineTextRange(1, 1, 0, 2)),
                new EndOfFileToken(new SingleLineTextRange(1, 3, 2, 0))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.EmptyCharacterLiteral, new SingleLineTextRange(1, 1, 0, 2))));
    }

    @Test
    public void charTooManyTest() {
        LexerOutput result = lex("'aa'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "a", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.TooManyCharsInCharLiteral, new SingleLineTextRange(1, 1, 0, 4))));
    }

    @Test
    public void charEscapeTest1() {
        LexerOutput result = lex("'\\''");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "'", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest2() {
        LexerOutput result = lex("'\"'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\"", new SingleLineTextRange(1, 1, 0, 3)),
                new EndOfFileToken(new SingleLineTextRange(1, 4, 3, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest3() {
        LexerOutput result = lex("'\\r'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\r", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest4() {
        LexerOutput result = lex("'\\n'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\n", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest5() {
        LexerOutput result = lex("'\\t'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\t", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest6() {
        LexerOutput result = lex("'\\b'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\b", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest7() {
        LexerOutput result = lex("'\\f'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\f", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest8() {
        LexerOutput result = lex("'\\\\'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\\", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest9() {
        LexerOutput result = lex("'\\u0001'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\u0001", new SingleLineTextRange(1, 1, 0, 8)),
                new EndOfFileToken(new SingleLineTextRange(1, 9, 8, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeErrorTest1() {
        LexerOutput result = lex("'\\a'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "a", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 2))));
    }

    @Test
    public void charEscapeErrorTest2() {
        LexerOutput result = lex("'\\u'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\0", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 2))));
    }

    @Test
    public void charEscapeErrorTest3() {
        LexerOutput result = lex("'\\uF'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\0", new SingleLineTextRange(1, 1, 0, 5)),
                new EndOfFileToken(new SingleLineTextRange(1, 6, 5, 0))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 3))));
    }

    @Test
    public void charEscapeErrorTest4() {
        LexerOutput result = lex("'\\uFA'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\0", new SingleLineTextRange(1, 1, 0, 6)),
                new EndOfFileToken(new SingleLineTextRange(1, 7, 6, 0))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 4))));
    }

    @Test
    public void charEscapeErrorTest5() {
        LexerOutput result = lex("'\\uFA0'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\0", new SingleLineTextRange(1, 1, 0, 7)),
                new EndOfFileToken(new SingleLineTextRange(1, 8, 7, 0))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 5))));
    }
}