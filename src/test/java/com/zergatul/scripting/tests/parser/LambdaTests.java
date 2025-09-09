package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.nodes.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class LambdaTests extends ParserTestBase {

    @Test
    public void lambdaTest1() {
        ParserOutput result = parse("func(() => 1);");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "func", new SingleLineTextRange(1, 1, 0, 4))),
                                                new ArgumentsListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 5, 4, 1)),
                                                        SeparatedList.of(
                                                                ExpressionNode.class,
                                                                new LambdaExpressionNode(
                                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 6, 5, 1)),
                                                                        SeparatedList.of(),
                                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 7, 6, 1))
                                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 8, 7, 1))),
                                                                        new Token(TokenType.EQUAL_GREATER, new SingleLineTextRange(1, 9, 8, 2))
                                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 11, 10, 1))),
                                                                        new ExpressionStatementNode(
                                                                                new IntegerLiteralExpressionNode(
                                                                                        null,
                                                                                        new ValueToken(TokenType.INTEGER_LITERAL, "1", new SingleLineTextRange(1, 12, 11, 1))),
                                                                                null))),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 13, 12, 1))),
                                                new SingleLineTextRange(1, 1, 0, 13)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 14, 13, 1)))),
                                new SingleLineTextRange(1, 1, 0, 14)),
                        new EndOfFileToken(new SingleLineTextRange(1, 15, 14, 0))),
                result.unit());
    }

    @Test
    public void lambdaTest2() {
        ParserOutput result = parse("func(a => {});");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "func", new SingleLineTextRange(1, 1, 0, 4))),
                                                new ArgumentsListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 5, 4, 1)),
                                                        SeparatedList.of(
                                                                ExpressionNode.class,
                                                                new LambdaExpressionNode(
                                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 6, 5, 0)),
                                                                        SeparatedList.of(
                                                                                NameExpressionNode.class,
                                                                                new NameExpressionNode(
                                                                                        new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 6, 5, 1))
                                                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 7, 6, 1))))),
                                                                        new ValueToken(TokenType.RIGHT_PARENTHESES, "", new SingleLineTextRange(1, 7, 6, 0)),
                                                                        new Token(TokenType.EQUAL_GREATER, new SingleLineTextRange(1, 8, 7, 2))
                                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 10, 9, 1))),
                                                                        new BlockStatementNode(
                                                                                new Token(TokenType.LEFT_CURLY_BRACKET, new SingleLineTextRange(1, 11, 10, 1)),
                                                                                List.of(),
                                                                                new Token(TokenType.RIGHT_CURLY_BRACKET, new SingleLineTextRange(1, 12, 11, 1))))),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 13, 12, 1))),
                                                new SingleLineTextRange(1, 1, 0, 13)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 14, 13, 1)))),
                                new SingleLineTextRange(1, 1, 0, 14)),
                        new EndOfFileToken(new SingleLineTextRange(1, 15, 14, 0))),
                result.unit());
    }

    @Test
    public void lambdaTest3() {
        ParserOutput result = parse("func((a, b, c) => {});");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "func", new SingleLineTextRange(1, 1, 0, 4))),
                                                new ArgumentsListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 5, 4, 1)),
                                                        SeparatedList.of(
                                                                ExpressionNode.class,
                                                                new LambdaExpressionNode(
                                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 6, 5, 1)),
                                                                        SeparatedList.of(
                                                                                NameExpressionNode.class,
                                                                                new NameExpressionNode(
                                                                                        new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 7, 6, 1))),
                                                                                new Token(TokenType.COMMA, new SingleLineTextRange(1, 8, 7, 1))
                                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 9, 8, 1))),
                                                                                new NameExpressionNode(
                                                                                        new ValueToken(TokenType.IDENTIFIER, "b", new SingleLineTextRange(1, 10, 9, 1))),
                                                                                new Token(TokenType.COMMA, new SingleLineTextRange(1, 11, 10, 1))
                                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 12, 11, 1))),
                                                                                new NameExpressionNode(
                                                                                        new ValueToken(TokenType.IDENTIFIER, "c", new SingleLineTextRange(1, 13, 12, 1)))),
                                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 14, 13, 1))
                                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 15, 14, 1))),
                                                                        new Token(TokenType.EQUAL_GREATER, new SingleLineTextRange(1, 16, 15, 2))
                                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 18, 17, 1))),
                                                                        new BlockStatementNode(
                                                                                new Token(TokenType.LEFT_CURLY_BRACKET, new SingleLineTextRange(1, 19, 18, 1)),
                                                                                List.of(),
                                                                                new Token(TokenType.RIGHT_CURLY_BRACKET, new SingleLineTextRange(1, 20, 19, 1))))),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 21, 20, 1))),
                                                new SingleLineTextRange(1, 1, 0, 21)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 22, 21, 1)))),
                                new SingleLineTextRange(1, 1, 0, 22)),
                        new EndOfFileToken(new SingleLineTextRange(1, 23, 22, 0))),
                result.unit());
    }
}