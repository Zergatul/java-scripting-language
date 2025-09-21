package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.lexer.Trivia;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.nodes.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class LambdaTests extends ParserTestBase {

    @Test
    public void lambdaTest1() {
        ParserOutput result = parse("func(() => 1);");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new ExpressionStatementNode(
                                new InvocationExpressionNode(
                                        new NameExpressionNode("func", new SingleLineTextRange(1, 1, 0, 4)),
                                        new ArgumentsListNode(List.of(
                                                new LambdaExpressionNode(
                                                        List.of(),
                                                        new Token(TokenType.EQUAL_GREATER, new SingleLineTextRange(1, 9, 8, 2))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 11, 10, 1))),
                                                        new ExpressionStatementNode(
                                                                new IntegerLiteralExpressionNode("1", new SingleLineTextRange(1, 12, 11, 1)),
                                                                new SingleLineTextRange(1, 12, 11, 1)),
                                                        new SingleLineTextRange(1, 6, 5, 7))),
                                                new SingleLineTextRange(1, 5, 4, 9)),
                                        new SingleLineTextRange(1, 1, 0, 13)),
                                new SingleLineTextRange(1, 1, 0, 14))),
                        new SingleLineTextRange(1, 1, 0, 14)),
                new SingleLineTextRange(1, 1, 0, 14)),
                result.unit());
    }

    @Test
    public void lambdaTest2() {
        ParserOutput result = parse("func(a => {});");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new ExpressionStatementNode(
                                new InvocationExpressionNode(
                                        new NameExpressionNode("func", new SingleLineTextRange(1, 1, 0, 4)),
                                        new ArgumentsListNode(List.of(
                                                new LambdaExpressionNode(
                                                        List.of(
                                                                new NameExpressionNode(
                                                                        new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 6, 5, 1)))),
                                                        new Token(TokenType.EQUAL_GREATER, new SingleLineTextRange(1, 8, 7, 2))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 10, 9, 1))),
                                                        new BlockStatementNode(
                                                                List.of(),
                                                                new SingleLineTextRange(1, 11, 10, 2)),
                                                        new SingleLineTextRange(1, 6, 5, 7))),
                                                new SingleLineTextRange(1, 5, 4, 9)),
                                        new SingleLineTextRange(1, 1, 0, 13)),
                                new SingleLineTextRange(1, 1, 0, 14))),
                        new SingleLineTextRange(1, 1, 0, 14)),
                new SingleLineTextRange(1, 1, 0, 14)),
                result.unit());
    }

    @Test
    public void lambdaTest3() {
        ParserOutput result = parse("func((a, b, c) => {});");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new ExpressionStatementNode(
                                new InvocationExpressionNode(
                                        new NameExpressionNode("func", new SingleLineTextRange(1, 1, 0, 4)),
                                        new ArgumentsListNode(List.of(
                                                new LambdaExpressionNode(
                                                        List.of(
                                                                new NameExpressionNode(
                                                                        new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 7, 6, 1))),
                                                                new NameExpressionNode(
                                                                        new ValueToken(TokenType.IDENTIFIER, "b", new SingleLineTextRange(1, 10, 9, 1))),
                                                                new NameExpressionNode(
                                                                        new ValueToken(TokenType.IDENTIFIER, "c", new SingleLineTextRange(1, 13, 12, 1)))),
                                                        new Token(TokenType.EQUAL_GREATER, new SingleLineTextRange(1, 16, 15, 2))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 18, 17, 1))),
                                                        new BlockStatementNode(
                                                                List.of(),
                                                                new SingleLineTextRange(1, 19, 18, 2)),
                                                        new SingleLineTextRange(1, 6, 5, 15))),
                                                new SingleLineTextRange(1, 5, 4, 17)),
                                        new SingleLineTextRange(1, 1, 0, 21)),
                                new SingleLineTextRange(1, 1, 0, 22))),
                        new SingleLineTextRange(1, 1, 0, 22)),
                new SingleLineTextRange(1, 1, 0, 22)),
                result.unit());
    }
}