package com.zergatul.scripting.tests.lexer;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class StringTests extends LexerTestBase {

    @Test
    public void stringTest1() {
        LexerOutput result = lex("\"\"\"test\"\"\\\"\\\"\\\"\\\"\"");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.STRING_LITERAL, "", new SingleLineTextRange(1, 1, 0, 2)),
                new ValueToken(TokenType.STRING_LITERAL, "test", new SingleLineTextRange(1, 3, 2, 6)),
                new ValueToken(TokenType.STRING_LITERAL, "\"\"\"\"", new SingleLineTextRange(1, 9, 8, 10)),
                new EndOfFileToken(new SingleLineTextRange(1, 19, 18, 0))),
                result.tokens());
    }

    @Test
    public void stringTest2() {
        LexerOutput result = lex("\"1\n");
        comparator.assertEquals(List.of(
                new DiagnosticMessage(LexerErrors.NewlineInString, new SingleLineTextRange(1, 1, 0, 2))),
                result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.STRING_LITERAL, "1", new SingleLineTextRange(1, 1, 0, 2))
                        .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(1, 3, 2, 1))),
                new EndOfFileToken(new SingleLineTextRange(2, 1, 3, 0))),
                result.tokens());
    }

    @Test
    public void stringTest3() {
        LexerOutput result = lex("\"");
        comparator.assertEquals(List.of(
                new DiagnosticMessage(LexerErrors.UnfinishedString, new SingleLineTextRange(1, 1, 0, 1))),
                result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.STRING_LITERAL, "", new SingleLineTextRange(1, 1, 0, 1)),
                new EndOfFileToken(new SingleLineTextRange(1, 2, 1, 0))),
                result.tokens());
    }

    @Test
    public void stringTest4() {
        LexerOutput result = lex("\"\\r\\n\"");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.STRING_LITERAL, "\r\n", new SingleLineTextRange(1, 1, 0, 6)),
                new EndOfFileToken(new SingleLineTextRange(1, 7, 6, 0))),
                result.tokens());
    }

    @Test
    public void stringTest5() {
        LexerOutput result = lex("\"\\aaa\\ccc\"");
        comparator.assertEquals(List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 2)),
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 6, 5, 2))),
                result.diagnostics());
        comparator.assertEquals(List.of(
                        new ValueToken(TokenType.STRING_LITERAL, "aaaccc", new SingleLineTextRange(1, 1, 0, 10)),
                        new EndOfFileToken(new SingleLineTextRange(1, 11, 10, 0))),
                result.tokens());
    }

    @Test
    public void stringTest6() {
        LexerOutput result = lex("\"\\u03C9\"");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.STRING_LITERAL, "ω", new SingleLineTextRange(1, 1, 0, 8)),
                new EndOfFileToken(new SingleLineTextRange(1, 9, 8, 0))),
                result.tokens());
    }

    @Test
    public void stringTest7() {
        LexerOutput result = lex("\"\\uD83D\\uDE00\"");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(List.of(
                new ValueToken(TokenType.STRING_LITERAL, "\uD83D\uDE00", new SingleLineTextRange(1, 1, 0, 14)),
                new EndOfFileToken(new SingleLineTextRange(1, 15, 14, 0))),
                result.tokens());
    }
}