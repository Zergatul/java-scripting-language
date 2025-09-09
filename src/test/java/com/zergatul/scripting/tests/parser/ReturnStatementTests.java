package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.EndOfFileToken;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.lexer.Trivia;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.nodes.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ReturnStatementTests extends ParserTestBase {

    @Test
    public void returnStatementTest1() {
        ParserOutput result = parse("return;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ReturnStatementNode(
                                        new Token(TokenType.RETURN, new SingleLineTextRange(1, 1, 0, 6)),
                                        null,
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 7, 6, 1)))),
                                new SingleLineTextRange(1, 1, 0, 7)),
                        new EndOfFileToken(new SingleLineTextRange(1, 8, 7, 0))),
                result.unit());
    }

    @Test
    public void returnStatementTest2() {
        ParserOutput result = parse("return true;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ReturnStatementNode(
                                        new Token(TokenType.RETURN, new SingleLineTextRange(1, 1, 0, 6))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 7, 6, 1))),
                                        new BooleanLiteralExpressionNode(
                                                new Token(TokenType.TRUE, new SingleLineTextRange(1, 8, 7, 4)),
                                                true),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 12, 11, 1)))),
                                new SingleLineTextRange(1, 1, 0, 12)),
                        new EndOfFileToken(new SingleLineTextRange(1, 13, 12, 0))),
                result.unit());
    }
}