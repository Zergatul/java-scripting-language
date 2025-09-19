package com.zergatul.scripting.tests.lexer;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class LexerTests {







    @Test
    public void identifierTest1() {
        LexerOutput result = lex("a");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 1, 0, 1))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void identifierTest2() {
        LexerOutput result = lex("_QwertY091");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.IDENTIFIER, "_QwertY091", new SingleLineTextRange(1, 1, 0, 10))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void invalidSymbolTest() {
        LexerOutput result = lex("\0");
        Assertions.assertEquals(result.tokens().size(), 0);
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.UnexpectedSymbol, new Token(TokenType.INVALID, new SingleLineTextRange(1, 1, 0, 1)), "0000")));
    }

    @Test
    public void invocationTest() {
        LexerOutput result = lex("abc.qwe.x();");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.IDENTIFIER, "abc", new SingleLineTextRange(1, 1, 0, 3)),
                new Token(TokenType.DOT, new SingleLineTextRange(1, 4, 3, 1)),
                new ValueToken(TokenType.IDENTIFIER, "qwe", new SingleLineTextRange(1, 5, 4, 3)),
                new Token(TokenType.DOT, new SingleLineTextRange(1, 8, 7, 1)),
                new ValueToken(TokenType.IDENTIFIER, "x", new SingleLineTextRange(1, 9, 8, 1)),
                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 10, 9, 1)),
                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 11, 10, 1)),
                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 12, 11, 1))
        ));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void assignmentOperatorsTest() {
        LexerOutput result = lex("+=-=*=/=");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.PLUS_EQUAL, new SingleLineTextRange(1, 1, 0, 2)),
                new Token(TokenType.MINUS_EQUAL, new SingleLineTextRange(1, 3, 2, 2)),
                new Token(TokenType.ASTERISK_EQUAL, new SingleLineTextRange(1, 5, 4, 2)),
                new Token(TokenType.SLASH_EQUAL, new SingleLineTextRange(1, 7, 6, 2))
        ));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void floatTest1() {
        LexerOutput result = lex("1.,.1,0.0,.1e+1,1e-2,1e3");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.FLOAT_LITERAL, "1.", new SingleLineTextRange(1, 1, 0, 2)),
                new Token(TokenType.COMMA, new SingleLineTextRange(1, 3, 2, 1)),
                new ValueToken(TokenType.FLOAT_LITERAL, ".1", new SingleLineTextRange(1, 4, 3, 2)),
                new Token(TokenType.COMMA, new SingleLineTextRange(1, 6, 5, 1)),
                new ValueToken(TokenType.FLOAT_LITERAL, "0.0", new SingleLineTextRange(1, 7, 6, 3)),
                new Token(TokenType.COMMA, new SingleLineTextRange(1, 10, 9, 1)),
                new ValueToken(TokenType.FLOAT_LITERAL, ".1e+1", new SingleLineTextRange(1, 11, 10, 5)),
                new Token(TokenType.COMMA, new SingleLineTextRange(1, 16, 15, 1)),
                new ValueToken(TokenType.FLOAT_LITERAL, "1e-2", new SingleLineTextRange(1, 17, 16, 4)),
                new Token(TokenType.COMMA, new SingleLineTextRange(1, 21, 20, 1)),
                new ValueToken(TokenType.FLOAT_LITERAL, "1e3", new SingleLineTextRange(1, 22, 21, 3))
        ));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void floatTest2() {
        String[] invalidNumbers = {
                ".1.",
                "1e",
                "1e+3m",
                "1x"
        };

        for (String input : invalidNumbers) {
            LexerOutput result = lex(input);
            Token token;
            Assertions.assertIterableEquals(result.tokens(), List.of(
                    token = new InvalidNumberToken(input, new SingleLineTextRange(1, 1, 0, input.length()))
            ));
            Assertions.assertIterableEquals(result.diagnostics(), List.of(
                    new DiagnosticMessage(LexerErrors.InvalidNumber, token, input)
            ));
        }
    }

    @Test
    public void stringTest1() {
        LexerOutput result = lex("\"\"\"test\"\"\\\"\\\"\\\"\\\"\"");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.STRING_LITERAL, "", new SingleLineTextRange(1, 1, 0, 2)),
                new ValueToken(TokenType.STRING_LITERAL, "test", new SingleLineTextRange(1, 3, 2, 6)),
                new ValueToken(TokenType.STRING_LITERAL, "\"\"\"\"", new SingleLineTextRange(1, 9, 8, 10))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void stringTest2() {
        LexerOutput result = lex("\"1\n");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.STRING_LITERAL, "1", new SingleLineTextRange(1, 1, 0, 2)),
                new Token(TokenType.LINE_BREAK, new SingleLineTextRange(1, 3, 2, 1))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.NewlineInString, new SingleLineTextRange(1, 1, 0, 2))));
    }

    @Test
    public void stringTest3() {
        LexerOutput result = lex("\"");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.STRING_LITERAL, "", new SingleLineTextRange(1, 1, 0, 1))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.UnfinishedString, new SingleLineTextRange(1, 1, 0, 1))));
    }

    @Test
    public void stringTest4() {
        LexerOutput result = lex("\"\\r\\n\"");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.STRING_LITERAL, "\r\n", new SingleLineTextRange(1, 1, 0, 6))));
        Assertions.assertTrue(result.diagnostics().isEmpty());
    }

    @Test
    public void stringTest5() {
        LexerOutput result = lex("\"\\aaa\\ccc\"");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.STRING_LITERAL, "aaaccc", new SingleLineTextRange(1, 1, 0, 10))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 2)),
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 6, 5, 2))));
    }

    @Test
    public void stringTest6() {
        LexerOutput result = lex("\"\\u03C9\"");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.STRING_LITERAL, "ω", new SingleLineTextRange(1, 1, 0, 8))));
        Assertions.assertTrue(result.diagnostics().isEmpty());
    }

    @Test
    public void stringTest7() {
        LexerOutput result = lex("\"\\uD83D\\uDE00\"");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.STRING_LITERAL, "\uD83D\uDE00", new SingleLineTextRange(1, 1, 0, 14))));
        Assertions.assertTrue(result.diagnostics().isEmpty());
    }

    @Test
    public void charEmptyTest() {
        LexerOutput result = lex("''");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, String.valueOf((char) 0), new SingleLineTextRange(1, 1, 0, 2))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.EmptyCharacterLiteral, new SingleLineTextRange(1, 1, 0, 2))));
    }

    @Test
    public void charTooManyTest() {
        LexerOutput result = lex("'aa'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "a", new SingleLineTextRange(1, 1, 0, 4))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.TooManyCharsInCharLiteral, new SingleLineTextRange(1, 1, 0, 4))));
    }

    @Test
    public void charEscapeTest1() {
        LexerOutput result = lex("'\\''");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "'", new SingleLineTextRange(1, 1, 0, 4))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest2() {
        LexerOutput result = lex("'\"'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\"", new SingleLineTextRange(1, 1, 0, 3))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest3() {
        LexerOutput result = lex("'\\r'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\r", new SingleLineTextRange(1, 1, 0, 4))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest4() {
        LexerOutput result = lex("'\\n'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\n", new SingleLineTextRange(1, 1, 0, 4))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest5() {
        LexerOutput result = lex("'\\t'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\t", new SingleLineTextRange(1, 1, 0, 4))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest6() {
        LexerOutput result = lex("'\\b'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\b", new SingleLineTextRange(1, 1, 0, 4))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest7() {
        LexerOutput result = lex("'\\f'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\f", new SingleLineTextRange(1, 1, 0, 4))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest8() {
        LexerOutput result = lex("'\\\\'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\\", new SingleLineTextRange(1, 1, 0, 4))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeTest9() {
        LexerOutput result = lex("'\\u0001'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\u0001", new SingleLineTextRange(1, 1, 0, 8))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void charEscapeErrorTest1() {
        LexerOutput result = lex("'\\a'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "a", new SingleLineTextRange(1, 1, 0, 4))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 2))));
    }

    @Test
    public void charEscapeErrorTest2() {
        LexerOutput result = lex("'\\u'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\0", new SingleLineTextRange(1, 1, 0, 4))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 2))));
    }

    @Test
    public void charEscapeErrorTest3() {
        LexerOutput result = lex("'\\uF'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\0", new SingleLineTextRange(1, 1, 0, 5))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 3))));
    }

    @Test
    public void charEscapeErrorTest4() {
        LexerOutput result = lex("'\\uFA'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\0", new SingleLineTextRange(1, 1, 0, 6))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 4))));
    }

    @Test
    public void charEscapeErrorTest5() {
        LexerOutput result = lex("'\\uFA0'");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.CHAR_LITERAL, "\0", new SingleLineTextRange(1, 1, 0, 7))));
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence, new SingleLineTextRange(1, 2, 1, 5))));
    }

    private LexerOutput lex(String code) {
        return new Lexer(new LexerInput(code)).lex();
    }
}