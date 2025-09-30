package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.ParserErrors;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.nodes.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ClassTests extends ParserTestBase {

    @Test
    public void unfinishedMemberTest() {
        ParserOutput result = parse("""
                class Region {
                    void
                }
                """);

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(ParserErrors.IdentifierExpected, new SingleLineTextRange(3, 1, 24, 1), "}"),
                        new DiagnosticMessage(ParserErrors.LeftParenthesisExpected, new SingleLineTextRange(3, 1, 24, 1), "}")),
                result.diagnostics());

        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(
                                new ClassNode(
                                        new Token(TokenType.CLASS, new SingleLineTextRange(1, 1, 0, 5))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 6, 5, 1))),
                                        new ValueToken(TokenType.IDENTIFIER, "Region", new SingleLineTextRange(1, 7, 6, 6))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 13, 12, 1))),
                                        new Token(TokenType.LEFT_CURLY_BRACKET, new SingleLineTextRange(1, 14, 13, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 15, 2, 1, 14, 1))),
                                        List.of(
                                                new ClassMethodNode(
                                                        new ModifiersNode(List.of(), new SingleLineTextRange(2, 5, 19, 0)),
                                                        new VoidTypeNode(
                                                                new Token(TokenType.VOID, new SingleLineTextRange(2, 5, 19, 4))
                                                                        .withLeadingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 1, 15, 4)))
                                                                        .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(2, 9, 3, 1, 23, 1)))),
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "", new SingleLineTextRange(2, 9, 23, 0))),
                                                        new ParameterListNode(
                                                                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 9, 23, 0)),
                                                                SeparatedList.of(),
                                                                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 9, 23, 0))),
                                                        null,
                                                        new BlockStatementNode(
                                                                new Token(TokenType.LEFT_CURLY_BRACKET, new SingleLineTextRange(2, 9, 23, 0)),
                                                                List.of(),
                                                                new Token(TokenType.RIGHT_CURLY_BRACKET, new SingleLineTextRange(2, 9, 23, 0))))),
                                        new Token(TokenType.RIGHT_CURLY_BRACKET, new SingleLineTextRange(3, 1, 24, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(3, 2, 4, 1, 25, 1))))),
                                new MultiLineTextRange(1, 1, 3, 2, 0, 25)),
                        new StatementsListNode(List.of(), new SingleLineTextRange(3, 2, 25, 0)),
                        new EndOfFileToken(new SingleLineTextRange(4, 1, 26, 0))),
                result.unit());
    }

    @Test
    public void wrongTokenTest() {
        ParserOutput result = parse("""
                class Region {
                    void Check())
                }
                """);

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(ParserErrors.CurlyBracketOrArrowExpected, new SingleLineTextRange(2, 17, 31, 1), ")"),
                        new DiagnosticMessage(ParserErrors.ClassMemberExpected, new SingleLineTextRange(2, 17, 31, 1), ")")),
                result.diagnostics());

        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(
                                new ClassNode(
                                        new Token(TokenType.CLASS, new SingleLineTextRange(1, 1, 0, 5))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 6, 5, 1))),
                                        new ValueToken(TokenType.IDENTIFIER, "Region", new SingleLineTextRange(1, 7, 6, 6))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 13, 12, 1))),
                                        new Token(TokenType.LEFT_CURLY_BRACKET, new SingleLineTextRange(1, 14, 13, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 15, 2, 1, 14, 1))),
                                        List.of(
                                                new ClassMethodNode(
                                                        new ModifiersNode(List.of(), new SingleLineTextRange(2, 5, 19, 0)),
                                                        new VoidTypeNode(
                                                                new Token(TokenType.VOID, new SingleLineTextRange(2, 5, 19, 4))
                                                                        .withLeadingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 1, 15, 4)))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 9, 23, 1)))),
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "Check", new SingleLineTextRange(2, 10, 24, 5))),
                                                        new ParameterListNode(
                                                                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 15, 29, 1)),
                                                                SeparatedList.of(),
                                                                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 16, 30, 1))),
                                                        null,
                                                        new BlockStatementNode(
                                                                new Token(TokenType.LEFT_CURLY_BRACKET, new SingleLineTextRange(2, 17, 31, 0)),
                                                                List.of(),
                                                                new Token(TokenType.RIGHT_CURLY_BRACKET, new SingleLineTextRange(2, 17, 31, 0))))),
                                        new Token(TokenType.RIGHT_CURLY_BRACKET, new SingleLineTextRange(3, 1, 33, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(3, 2, 4, 1, 34, 1))))),
                                new MultiLineTextRange(1, 1, 3, 2, 0, 34)),
                        new StatementsListNode(List.of(), new SingleLineTextRange(3, 2, 34, 0)),
                        new EndOfFileToken(new SingleLineTextRange(4, 1, 35, 0))),
                result.unit());
    }
}