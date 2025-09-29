package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.AssignmentOperator;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.nodes.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class IfStatementTests extends ParserTestBase {

    @Test
    public void ifStatementTest1() {
        ParserOutput result = parse("if (a) b = 3;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new IfStatementNode(
                                        new Token(TokenType.IF, new SingleLineTextRange(1, 1, 0, 2))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 3, 2, 1))),
                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 4, 3, 1)),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 5, 4, 1))),
                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 6, 5, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 7, 6, 1))),
                                        new AssignmentStatementNode(
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "b", new SingleLineTextRange(1, 8, 7, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 9, 8, 1)))),
                                                new AssignmentOperatorNode(
                                                        new Token(TokenType.EQUAL, new SingleLineTextRange(1, 10, 9, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 11, 10, 1))),
                                                        AssignmentOperator.ASSIGNMENT,
                                                        new SingleLineTextRange(1, 10, 9, 1)),
                                                new IntegerLiteralExpressionNode(
                                                        null,
                                                        new ValueToken(TokenType.INTEGER_LITERAL, "3", new SingleLineTextRange(1, 12, 11, 1))),
                                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 13, 12, 1)),
                                                new SingleLineTextRange(1, 8, 7, 6)),
                                        null,
                                        null,
                                        new SingleLineTextRange(1, 1, 0, 13))),
                                new SingleLineTextRange(1, 1, 0, 13)),
                        new EndOfFileToken(new SingleLineTextRange(1, 14, 13, 0))),
                result.unit());
    }

    @Test
    public void ifStatementTest2() {
        ParserOutput result = parse("if (a) b = 3;else{}");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new IfStatementNode(
                                        new Token(TokenType.IF, new SingleLineTextRange(1, 1, 0, 2))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 3, 2, 1))),
                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 4, 3, 1)),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 5, 4, 1))),
                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 6, 5, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 7, 6, 1))),
                                        new AssignmentStatementNode(
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "b", new SingleLineTextRange(1, 8, 7, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 9, 8, 1)))),
                                                new AssignmentOperatorNode(
                                                        new Token(TokenType.EQUAL, new SingleLineTextRange(1, 10, 9, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 11, 10, 1))),
                                                        AssignmentOperator.ASSIGNMENT,
                                                        new SingleLineTextRange(1, 10, 9, 1)),
                                                new IntegerLiteralExpressionNode(
                                                        null,
                                                        new ValueToken(TokenType.INTEGER_LITERAL, "3", new SingleLineTextRange(1, 12, 11, 1))),
                                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 13, 12, 1)),
                                                new SingleLineTextRange(1, 8, 7, 6)),
                                        new Token(TokenType.ELSE, new SingleLineTextRange(1, 14, 13, 4)),
                                        new BlockStatementNode(
                                                new Token(TokenType.LEFT_CURLY_BRACKET, new SingleLineTextRange(1, 18, 17, 1)),
                                                List.of(),
                                                new Token(TokenType.RIGHT_CURLY_BRACKET, new SingleLineTextRange(1, 19, 18, 1))),
                                        new SingleLineTextRange(1, 1, 0, 19))),
                                new SingleLineTextRange(1, 1, 0, 19)),
                        new EndOfFileToken(new SingleLineTextRange(1, 20, 19, 0))),
                result.unit());
    }
}