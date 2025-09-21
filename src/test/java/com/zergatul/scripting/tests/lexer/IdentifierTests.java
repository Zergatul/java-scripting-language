package com.zergatul.scripting.tests.lexer;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class IdentifierTests extends LexerTestBase {

    @Test
    public void identifierTest1() {
        LexerOutput result = lex("a");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 1, 0, 1)),
                new EndOfFileToken(new SingleLineTextRange(1, 2, 1, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }

    @Test
    public void identifierTest2() {
        LexerOutput result = lex("_QwertY091");
        Assertions.assertIterableEquals(result.tokens(), List.of(
                new ValueToken(TokenType.IDENTIFIER, "_QwertY091", new SingleLineTextRange(1, 1, 0, 10)),
                new EndOfFileToken(new SingleLineTextRange(1, 11, 10, 0))));
        Assertions.assertEquals(result.diagnostics().size(), 0);
    }
}