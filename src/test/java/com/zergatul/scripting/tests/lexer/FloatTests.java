package com.zergatul.scripting.tests.lexer;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class FloatTests extends LexerTestBase {

    @Test
    public void floatTest1() {
        LexerOutput result = lex("1.,.1,0.0,.1e+1,1e-2,1e3");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(List.of(
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
                new ValueToken(TokenType.FLOAT_LITERAL, "1e3", new SingleLineTextRange(1, 22, 21, 3)),
                new EndOfFileToken(new SingleLineTextRange(1, 25, 24, 0))),
                result.tokens());
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
            Token token = new InvalidNumberToken(input, new SingleLineTextRange(1, 1, 0, input.length()));
            comparator.assertEquals(List.of(
                    new DiagnosticMessage(LexerErrors.InvalidNumber, token, input)),
                    result.diagnostics());
            comparator.assertEquals(List.of(
                    token,
                    new EndOfFileToken(new SingleLineTextRange(1, 1 + input.length(), input.length(), 0))),
                    result.tokens());
        }
    }
}