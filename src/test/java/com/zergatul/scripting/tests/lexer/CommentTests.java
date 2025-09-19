package com.zergatul.scripting.tests.lexer;

import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CommentTests {

    @Test
    public void singleLineCommentTest1() {
        LexerOutput result = lex("//");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new EndOfFileToken(new SingleLineTextRange(1, 3, 2, 0))
                        .withLeadingTrivia(new Trivia(TokenType.SINGLE_LINE_COMMENT, new SingleLineTextRange(1, 1, 0, 2)))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void singleLineCommentTest2() {
        LexerOutput result = lex("//abc");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new EndOfFileToken(new SingleLineTextRange(1, 6, 5, 0))
                        .withLeadingTrivia(new Trivia(TokenType.SINGLE_LINE_COMMENT, new SingleLineTextRange(1, 1, 0, 5)))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void singleLineCommentTest3() {
        LexerOutput result = lex("//abc\r[");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(2, 1, 6, 1))
                        .withLeadingTrivia(new Trivia(TokenType.SINGLE_LINE_COMMENT, new SingleLineTextRange(1, 1, 0, 5)))
                        .withLeadingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(1, 6,5, 1))),
                new EndOfFileToken(new SingleLineTextRange(2, 2, 7, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void singleLineCommentTest4() {
        LexerOutput result = lex("//abc\r\n[");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(2, 1, 7, 1))
                        .withLeadingTrivia(new Trivia(TokenType.SINGLE_LINE_COMMENT, new SingleLineTextRange(1, 1, 0, 5)))
                        .withLeadingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(1, 6,5, 2))),
                new EndOfFileToken(new SingleLineTextRange(2, 2, 8, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void singleLineCommentTest5() {
        LexerOutput result = lex("//abc\n[");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(2, 1, 6, 1))
                        .withLeadingTrivia(new Trivia(TokenType.SINGLE_LINE_COMMENT, new SingleLineTextRange(1, 1, 0, 5)))
                        .withLeadingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(1, 6,5, 1))),
                new EndOfFileToken(new SingleLineTextRange(2, 2, 7, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void multiLineCommentTest1() {
        LexerOutput result = lex("/*");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new EndOfFileToken(new SingleLineTextRange(1, 3, 2, 0))
                        .withLeadingTrivia(new Trivia(TokenType.MULTI_LINE_COMMENT, new SingleLineTextRange(1, 1, 0, 2)))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void multiLineCommentTest2() {
        LexerOutput result = lex("/*/[");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new EndOfFileToken(new SingleLineTextRange(1, 5, 4, 0))
                        .withLeadingTrivia(new Trivia(TokenType.MULTI_LINE_COMMENT, new SingleLineTextRange(1, 1, 0, 4)))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void multiLineCommentTest3() {
        LexerOutput result = lex("/**/[");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(1, 5, 4, 1))
                        .withLeadingTrivia(new Trivia(TokenType.MULTI_LINE_COMMENT, new SingleLineTextRange(1, 1, 0, 4))),
                new EndOfFileToken(new SingleLineTextRange(1, 6, 5, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void multiLineCommentTest4() {
        LexerOutput result = lex("/*\n*/[");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(2, 3, 5, 1))
                        .withLeadingTrivia(new Trivia(TokenType.MULTI_LINE_COMMENT, new MultiLineTextRange(1, 1, 2, 3, 0, 5))),
                new EndOfFileToken(new SingleLineTextRange(2, 4, 6, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void multiLineCommentTest5() {
        LexerOutput result = lex("/*\r\r\n\r\n*/[");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(4, 3, 9, 1))
                        .withLeadingTrivia(new Trivia(TokenType.MULTI_LINE_COMMENT, new MultiLineTextRange(1, 1, 4, 3, 0, 9))),
                new EndOfFileToken(new SingleLineTextRange(4, 4, 10, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    private LexerOutput lex(String code) {
        return new Lexer(new LexerInput(code)).lex();
    }
}
