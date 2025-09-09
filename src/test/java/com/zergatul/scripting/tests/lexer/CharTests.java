package com.zergatul.scripting.tests.lexer;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CharTests extends LexerTestBase {

    @Test
    public void charEmptyTest() {
        LexerOutput result = lex("''");
        comparator.assertEquals(List.of(
                new DiagnosticMessage(LexerErrors.EmptyCharacterLiteral, new SingleLineTextRange(1, 1, 0, 2))),
                result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, String.valueOf((char) 0), new SingleLineTextRange(1, 1, 0, 2)),
                new EndOfFileToken(new SingleLineTextRange(1, 3, 2, 0))),
                result.tokens());
    }

    @Test
    public void charTooManyTest() {
        LexerOutput result = lex("'aa'");
        comparator.assertEquals(List.of(
                new DiagnosticMessage(LexerErrors.TooManyCharsInCharLiteral, new SingleLineTextRange(1, 1, 0, 4))),
                result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "a", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))),
                result.tokens());
    }

    @Test
    public void charEscapeTest1() {
        LexerOutput result = lex("'\\''");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "'", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))),
                result.tokens());
    }

    @Test
    public void charEscapeTest2() {
        LexerOutput result = lex("'\"'");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\"", new SingleLineTextRange(1, 1, 0, 3)),
                new EndOfFileToken(new SingleLineTextRange(1, 4, 3, 0))),
                result.tokens());
    }

    @Test
    public void charEscapeTest3() {
        LexerOutput result = lex("'\\r'");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\r", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))),
                result.tokens());
    }

    @Test
    public void charEscapeTest4() {
        LexerOutput result = lex("'\\n'");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\n", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))),
                result.tokens());
    }

    @Test
    public void charEscapeTest5() {
        LexerOutput result = lex("'\\t'");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\t", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))),
                result.tokens());
    }

    @Test
    public void charEscapeTest6() {
        LexerOutput result = lex("'\\b'");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\b", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))),
                result.tokens());
    }

    @Test
    public void charEscapeTest7() {
        LexerOutput result = lex("'\\f'");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\f", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))),
                result.tokens());
    }

    @Test
    public void charEscapeTest8() {
        LexerOutput result = lex("'\\\\'");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\\", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))),
                result.tokens());
    }

    @Test
    public void charEscapeTest9() {
        LexerOutput result = lex("'\\u0001'");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\u0001", new SingleLineTextRange(1, 1, 0, 8)),
                new EndOfFileToken(new SingleLineTextRange(1, 9, 8, 0))),
                result.tokens());
    }

    @Test
    public void charEscapeErrorTest1() {
        LexerOutput result = lex("'\\a'");
        comparator.assertEquals(List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 2))),
                result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "a", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))),
                result.tokens());
    }

    @Test
    public void charEscapeErrorTest2() {
        LexerOutput result = lex("'\\u'");
        comparator.assertEquals(List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 2))),
                result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\0", new SingleLineTextRange(1, 1, 0, 4)),
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))),
                result.tokens());
    }

    @Test
    public void charEscapeErrorTest3() {
        LexerOutput result = lex("'\\uF'");
        comparator.assertEquals(List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 3))),
                result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\0", new SingleLineTextRange(1, 1, 0, 5)),
                new EndOfFileToken(new SingleLineTextRange(1, 6, 5, 0))),
                result.tokens());
    }

    @Test
    public void charEscapeErrorTest4() {
        LexerOutput result = lex("'\\uFA'");
        comparator.assertEquals(List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 4))),
                result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\0", new SingleLineTextRange(1, 1, 0, 6)),
                new EndOfFileToken(new SingleLineTextRange(1, 7, 6, 0))),
                result.tokens());
    }

    @Test
    public void charEscapeErrorTest5() {
        LexerOutput result = lex("'\\uFA0'");
        comparator.assertEquals(List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 5))),
                result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\0", new SingleLineTextRange(1, 1, 0, 7)),
                new EndOfFileToken(new SingleLineTextRange(1, 8, 7, 0))),
                result.tokens());
    }
}