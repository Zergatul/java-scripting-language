package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.lexer.Trivia;
import com.zergatul.scripting.parser.*;
import com.zergatul.scripting.parser.nodes.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BasicTests extends ParserTestBase {

    @Test
    public void emptyCodeTest() {
        ParserOutput result = parse("");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new SingleLineTextRange(1, 1, 0, 0)),
                result.unit());
    }

    @Test
    public void emptyBlockStatementTest() {
        ParserOutput result = parse("{}");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new BlockStatementNode(
                                List.of(),
                                new SingleLineTextRange(1, 1, 0, 2))),
                        new SingleLineTextRange(1, 1, 0, 2)),
                new SingleLineTextRange(1, 1, 0, 2)),
                result.unit());
    }

    @Test
    public void simpleVariableDeclarationTest() {
        ParserOutput result = parse("int x;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new VariableDeclarationNode(
                                new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 1, 0, 3)),
                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
                                null,
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 6, 5, 1)),
                                new SingleLineTextRange(1, 1, 0, 6))),
                        new SingleLineTextRange(1, 1, 0, 6)),
                new SingleLineTextRange(1, 1, 0, 6)),
                result.unit());
    }

    @Test
    public void simpleVariableDeclarationWithInitializerTest() {
        ParserOutput result = parse("int x = 10;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new VariableDeclarationNode(
                                new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 1, 0, 3)),
                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
                                new IntegerLiteralExpressionNode("10", new SingleLineTextRange(1, 9, 8, 2)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 11, 10, 1)),
                                new SingleLineTextRange(1, 1, 0, 11))),
                        new SingleLineTextRange(1, 1, 0, 11)),
                new SingleLineTextRange(1, 1, 0, 11)),
                result.unit());
    }

    @Test
    public void arrayVariableDeclarationTest() {
        ParserOutput result = parse("int[][] x;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new VariableDeclarationNode(
                                new ArrayTypeNode(
                                        new ArrayTypeNode(
                                                new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 1, 0, 3)),
                                                new SingleLineTextRange(1, 1, 0, 5)),
                                        new SingleLineTextRange(1, 1, 0, 7)),
                                new NameExpressionNode("x", new SingleLineTextRange(1, 9, 8, 1)),
                                null,
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 10, 9, 1)),
                                new SingleLineTextRange(1, 1, 0, 10))),
                        new SingleLineTextRange(1, 1, 0, 10)),
                new SingleLineTextRange(1, 1, 0, 10)),
                result.unit());
    }

    @Test
    public void simpleAssignmentTest() {
        ParserOutput result = parse("a = b + c;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(new AssignmentStatementNode(
                        new NameExpressionNode("a", new SingleLineTextRange(1, 1, 0, 1)),
                        new AssignmentOperatorNode(AssignmentOperator.ASSIGNMENT, new SingleLineTextRange(1, 3, 2, 1)),
                        new BinaryExpressionNode(
                                new NameExpressionNode("b", new SingleLineTextRange(1, 5, 4, 1)),
                                new BinaryOperatorNode(BinaryOperator.PLUS, new SingleLineTextRange(1, 7, 6, 1)),
                                new NameExpressionNode("c", new SingleLineTextRange(1, 9, 8, 1)),
                                new SingleLineTextRange(1, 5, 4, 5)),
                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 10, 9, 1)),
                        new SingleLineTextRange(1, 1, 0, 10))),
                        new SingleLineTextRange(1, 1, 0, 10)),
                new SingleLineTextRange(1, 1, 0, 10)),
                result.unit());
    }

    @Test
    public void assignmentOperatorsTest() {
        ParserOutput result = parse("a += 10; b -= 15; c *= 2; d /= 2;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new AssignmentStatementNode(
                                new NameExpressionNode("a", new SingleLineTextRange(1, 1, 0, 1)),
                                new AssignmentOperatorNode(AssignmentOperator.PLUS_ASSIGNMENT, new SingleLineTextRange(1, 3, 2, 2)),
                                new IntegerLiteralExpressionNode("10", new SingleLineTextRange(1, 6, 5, 2)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 8, 7, 1))
                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 9, 8, 1))),
                                new SingleLineTextRange(1, 1, 0, 8)),
                        new AssignmentStatementNode(
                                new NameExpressionNode("b", new SingleLineTextRange(1, 10, 9, 1)),
                                new AssignmentOperatorNode(AssignmentOperator.MINUS_ASSIGNMENT, new SingleLineTextRange(1, 12, 11, 2)),
                                new IntegerLiteralExpressionNode("15", new SingleLineTextRange(1, 15, 14, 2)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 17, 16, 1))
                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 18, 17, 1))),
                                new SingleLineTextRange(1, 10, 9, 8)),
                        new AssignmentStatementNode(
                                new NameExpressionNode("c", new SingleLineTextRange(1, 19, 18, 1)),
                                new AssignmentOperatorNode(AssignmentOperator.MULTIPLY_ASSIGNMENT, new SingleLineTextRange(1, 21, 20, 2)),
                                new IntegerLiteralExpressionNode("2", new SingleLineTextRange(1, 24, 23, 1)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 25, 24, 1))
                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 26, 25, 1))),
                                new SingleLineTextRange(1, 19, 18, 7)),
                        new AssignmentStatementNode(
                                new NameExpressionNode("d", new SingleLineTextRange(1, 27, 26, 1)),
                                new AssignmentOperatorNode(AssignmentOperator.DIVIDE_ASSIGNMENT, new SingleLineTextRange(1, 29, 28, 2)),
                                new IntegerLiteralExpressionNode("2", new SingleLineTextRange(1, 32, 31, 1)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 33, 32, 1)),
                                new SingleLineTextRange(1, 27, 26, 7))),
                        new SingleLineTextRange(1, 1, 0, 33)),
                new SingleLineTextRange(1, 1, 0, 33)),
                result.unit());
    }

    @Test
    public void invokeExpressionTest() {
        ParserOutput result = parse("abc.qwe.x();");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new ExpressionStatementNode(
                                new InvocationExpressionNode(
                                        new MemberAccessExpressionNode(
                                                new MemberAccessExpressionNode(
                                                        new NameExpressionNode("abc", new SingleLineTextRange(1, 1, 0, 3)),
                                                        new Token(TokenType.DOT, new SingleLineTextRange(1, 4, 3, 1)),
                                                        new NameExpressionNode("qwe", new SingleLineTextRange(1, 5, 4, 3)),
                                                        new SingleLineTextRange(1, 1, 0, 7)),
                                                new Token(TokenType.DOT, new SingleLineTextRange(1, 8, 7, 1)),
                                                new NameExpressionNode("x", new SingleLineTextRange(1, 9, 8, 1)),
                                                new SingleLineTextRange(1, 1, 0, 9)),
                                        new ArgumentsListNode(List.of(), new SingleLineTextRange(1, 10, 9, 2)),
                                        new SingleLineTextRange(1, 1, 0, 11)),
                                new SingleLineTextRange(1, 1, 0, 12))),
                        new SingleLineTextRange(1, 1, 0, 12)),
                new SingleLineTextRange(1, 1, 0, 12)),
                result.unit());
    }

    @Test
    public void unaryOperatorTest() {
        ParserOutput result = parse("a = -b;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new AssignmentStatementNode(
                                new NameExpressionNode("a", new SingleLineTextRange(1, 1, 0, 1)),
                                new AssignmentOperatorNode(AssignmentOperator.ASSIGNMENT, new SingleLineTextRange(1, 3, 2, 1)),
                                new UnaryExpressionNode(
                                        new UnaryOperatorNode(UnaryOperator.MINUS, new SingleLineTextRange(1, 5, 4, 1)),
                                        new NameExpressionNode("b", new SingleLineTextRange(1, 6, 5, 1)),
                                        new SingleLineTextRange(1, 5, 4, 2)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 7, 6, 1)),
                                new SingleLineTextRange(1, 1, 0, 7))),
                        new SingleLineTextRange(1, 1, 0, 7)),
                new SingleLineTextRange(1, 1, 0, 7)),
                result.unit());
    }

    @Test
    public void negativeIntegerTest() {
        ParserOutput result = parse("x = -100;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new AssignmentStatementNode(
                                new NameExpressionNode("x", new SingleLineTextRange(1, 1, 0, 1)),
                                new AssignmentOperatorNode(AssignmentOperator.ASSIGNMENT, new SingleLineTextRange(1, 3, 2, 1)),
                                new IntegerLiteralExpressionNode("-100", new SingleLineTextRange(1, 5, 4, 4)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 9, 8, 1)),
                                new SingleLineTextRange(1, 1, 0, 9))),
                        new SingleLineTextRange(1, 1, 0, 9)),
                new SingleLineTextRange(1, 1, 0, 9)),
                result.unit());
    }

    @Test
    public void unaryMinusTest() {
        ParserOutput result = parse("x = -1 + 1;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new AssignmentStatementNode(
                                new NameExpressionNode("x", new SingleLineTextRange(1, 1, 0, 1)),
                                new AssignmentOperatorNode(AssignmentOperator.ASSIGNMENT, new SingleLineTextRange(1, 3, 2, 1)),
                                new BinaryExpressionNode(
                                        new IntegerLiteralExpressionNode("-1", new SingleLineTextRange(1, 5, 4, 2)),
                                        new BinaryOperatorNode(BinaryOperator.PLUS, new SingleLineTextRange(1, 8, 7, 1)),
                                        new IntegerLiteralExpressionNode("1", new SingleLineTextRange(1, 10, 9, 1)),
                                        new SingleLineTextRange(1, 5, 4, 6)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 11, 10, 1)),
                                new SingleLineTextRange(1, 1, 0, 11))),
                        new SingleLineTextRange(1, 1, 0, 11)),
                new SingleLineTextRange(1, 1, 0, 11)),
                result.unit());
    }
}