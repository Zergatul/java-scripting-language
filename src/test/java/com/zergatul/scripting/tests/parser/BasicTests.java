package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.*;
import com.zergatul.scripting.parser.nodes.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BasicTests extends ParserTestBase {

    @Test
    public void emptyCodeTest() {
        ParserOutput result = parse("");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new EndOfFileToken(new SingleLineTextRange(1, 1, 0, 0))),
                result.unit());
    }

    @Test
    public void emptyBlockStatementTest() {
        ParserOutput result = parse("{}");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new BlockStatementNode(
                                        new Token(TokenType.LEFT_CURLY_BRACKET, new SingleLineTextRange(1, 1, 0, 1)),
                                        List.of(),
                                        new Token(TokenType.RIGHT_CURLY_BRACKET, new SingleLineTextRange(1, 2, 1, 1)))),
                                new SingleLineTextRange(1, 1, 0, 2)),
                        new EndOfFileToken(new SingleLineTextRange(1, 3, 2, 0))),
                result.unit());
    }

    @Test
    public void simpleVariableDeclarationTest() {
        ParserOutput result = parse("int x;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new VariableDeclarationNode(
                                        new PredefinedTypeNode(
                                                new Token(TokenType.INT, new SingleLineTextRange(1, 1, 0, 3))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 4, 3, 1))),
                                                PredefinedType.INT),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "x", new SingleLineTextRange(1, 5, 4, 1))),
                                        null,
                                        null,
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 6, 5, 1)))),
                                new SingleLineTextRange(1, 1, 0, 6)),
                        new EndOfFileToken(new SingleLineTextRange(1, 7, 6, 0))),
                result.unit());
    }

    @Test
    public void simpleVariableDeclarationWithInitializerTest() {
        ParserOutput result = parse("int x = 10;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new VariableDeclarationNode(
                                        new PredefinedTypeNode(
                                                new Token(TokenType.INT, new SingleLineTextRange(1, 1, 0, 3))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 4, 3, 1))),
                                                PredefinedType.INT),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "x", new SingleLineTextRange(1, 5, 4, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 6, 5, 1)))),
                                        new Token(TokenType.EQUAL, new SingleLineTextRange(1, 7, 6, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 8, 7, 1))),
                                        new IntegerLiteralExpressionNode(
                                                null,
                                                new ValueToken(TokenType.INTEGER_LITERAL, "10", new SingleLineTextRange(1, 9, 8, 2))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 11, 10, 1)))),
                                new SingleLineTextRange(1, 1, 0, 11)),
                        new EndOfFileToken(new SingleLineTextRange(1, 12, 11, 0))),
                result.unit());
    }

    @Test
    public void arrayVariableDeclarationTest() {
        ParserOutput result = parse("int[][] x;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new VariableDeclarationNode(
                                        new ArrayTypeNode(
                                                new ArrayTypeNode(
                                                        new PredefinedTypeNode(
                                                                new Token(TokenType.INT, new SingleLineTextRange(1, 1, 0, 3)),
                                                                PredefinedType.INT),
                                                        new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(1, 4, 3, 1)),
                                                        new Token(TokenType.RIGHT_SQUARE_BRACKET, new SingleLineTextRange(1, 5, 4, 1)),
                                                        new SingleLineTextRange(1, 1, 0, 5)),
                                                new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(1, 6, 5, 1)),
                                                new Token(TokenType.RIGHT_SQUARE_BRACKET, new SingleLineTextRange(1, 7, 6, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 8, 7, 1))),
                                                new SingleLineTextRange(1, 1, 0, 7)),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "x", new SingleLineTextRange(1, 9, 8, 1))),
                                        null,
                                        null,
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 10, 9, 1)))),
                                new SingleLineTextRange(1, 1, 0, 10)),
                        new EndOfFileToken(new SingleLineTextRange(1, 11, 10, 0))),
                result.unit());
    }

    @Test
    public void simpleAssignmentTest() {
        ParserOutput result = parse("a = b + c;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new AssignmentStatementNode(
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 1, 0, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 2, 1, 1)))),
                                        new AssignmentOperatorNode(
                                                new Token(TokenType.EQUAL, new SingleLineTextRange(1, 3, 2, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 4, 3, 1))),
                                                AssignmentOperator.ASSIGNMENT,
                                                new SingleLineTextRange(1, 3, 2, 1)),
                                        new BinaryExpressionNode(
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "b", new SingleLineTextRange(1, 5, 4, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 6, 5, 1)))),
                                                new BinaryOperatorNode(
                                                        new Token(TokenType.PLUS, new SingleLineTextRange(1, 7, 6, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 8, 7, 1))),
                                                        BinaryOperator.PLUS),
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "c", new SingleLineTextRange(1, 9, 8, 1))),
                                                new SingleLineTextRange(1, 5, 4, 5)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 10, 9, 1)),
                                        new SingleLineTextRange(1, 1, 0, 10))),
                                new SingleLineTextRange(1, 1, 0, 10)),
                        new EndOfFileToken(new SingleLineTextRange(1, 11, 10, 0))),
                result.unit());
    }

    @Test
    public void assignmentOperatorsTest() {
        ParserOutput result = parse("a += 10; b -= 15; c *= 2; d /= 2;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new AssignmentStatementNode(
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 1, 0, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 2, 1, 1)))),
                                        new AssignmentOperatorNode(
                                                new Token(TokenType.PLUS_EQUAL, new SingleLineTextRange(1, 3, 2, 2))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 5, 4, 1))),
                                                AssignmentOperator.PLUS_ASSIGNMENT,
                                                new SingleLineTextRange(1, 3, 2, 2)),
                                        new IntegerLiteralExpressionNode(
                                                null,
                                                new ValueToken(TokenType.INTEGER_LITERAL, "10", new SingleLineTextRange(1, 6, 5, 2))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 8, 7, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 9, 8, 1))),
                                        new SingleLineTextRange(1, 1, 0, 8)),
                                new AssignmentStatementNode(
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "b", new SingleLineTextRange(1, 10, 9, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 11, 10, 1)))),
                                        new AssignmentOperatorNode(
                                                new Token(TokenType.MINUS_EQUAL, new SingleLineTextRange(1, 12, 11, 2))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 14, 13, 1))),
                                                AssignmentOperator.MINUS_ASSIGNMENT,
                                                new SingleLineTextRange(1, 12, 11, 2)),
                                        new IntegerLiteralExpressionNode(
                                                null,
                                                new ValueToken(TokenType.INTEGER_LITERAL, "15", new SingleLineTextRange(1, 15, 14, 2))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 17, 16, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 18, 17, 1))),
                                        new SingleLineTextRange(1, 10, 9, 8)),
                                new AssignmentStatementNode(
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "c", new SingleLineTextRange(1, 19, 18, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 20, 19, 1)))),
                                        new AssignmentOperatorNode(
                                                new Token(TokenType.ASTERISK_EQUAL, new SingleLineTextRange(1, 21, 20, 2))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 23, 22, 1))),
                                                AssignmentOperator.MULTIPLY_ASSIGNMENT,
                                                new SingleLineTextRange(1, 21, 20, 2)),
                                        new IntegerLiteralExpressionNode(
                                                null,
                                                new ValueToken(TokenType.INTEGER_LITERAL, "2", new SingleLineTextRange(1, 24, 23, 1))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 25, 24, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 26, 25, 1))),
                                        new SingleLineTextRange(1, 19, 18, 7)),
                                new AssignmentStatementNode(
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "d", new SingleLineTextRange(1, 27, 26, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 28, 27, 1)))),
                                        new AssignmentOperatorNode(
                                                new Token(TokenType.SLASH_EQUAL, new SingleLineTextRange(1, 29, 28, 2))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 31, 30, 1))),
                                                AssignmentOperator.DIVIDE_ASSIGNMENT,
                                                new SingleLineTextRange(1, 29, 28, 2)),
                                        new IntegerLiteralExpressionNode(
                                                null,
                                                new ValueToken(TokenType.INTEGER_LITERAL, "2", new SingleLineTextRange(1, 32, 31, 1))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 33, 32, 1)),
                                        new SingleLineTextRange(1, 27, 26, 7))),
                                new SingleLineTextRange(1, 1, 0, 33)),
                        new EndOfFileToken(new SingleLineTextRange(1, 34, 33, 0))),
                result.unit());
    }

    @Test
    public void invokeExpressionTest() {
        ParserOutput result = parse("abc.qwe.x();");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new MemberAccessExpressionNode(
                                                        new MemberAccessExpressionNode(
                                                                new NameExpressionNode(
                                                                        new ValueToken(TokenType.IDENTIFIER, "abc", new SingleLineTextRange(1, 1, 0, 3))),
                                                                new Token(TokenType.DOT, new SingleLineTextRange(1, 4, 3, 1)),
                                                                new NameExpressionNode(
                                                                        new ValueToken(TokenType.IDENTIFIER, "qwe", new SingleLineTextRange(1, 5, 4, 3)))),
                                                        new Token(TokenType.DOT, new SingleLineTextRange(1, 8, 7, 1)),
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "x", new SingleLineTextRange(1, 9, 8, 1)))),
                                                new ArgumentsListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 10, 9, 1)),
                                                        SeparatedList.of(),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 11, 10, 1))),
                                                new SingleLineTextRange(1, 1, 0, 11)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 12, 11, 1)))),
                                new SingleLineTextRange(1, 1, 0, 12)),
                        new EndOfFileToken(new SingleLineTextRange(1, 13, 12, 0))),
                result.unit());
    }

    @Test
    public void unaryOperatorTest() {
        ParserOutput result = parse("a = -b;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new AssignmentStatementNode(
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 1, 0, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 2, 1, 1)))),
                                        new AssignmentOperatorNode(
                                                new Token(TokenType.EQUAL, new SingleLineTextRange(1, 3, 2, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 4, 3, 1))),
                                                AssignmentOperator.ASSIGNMENT,
                                                new SingleLineTextRange(1, 3, 2, 1)),
                                        new UnaryExpressionNode(
                                                new UnaryOperatorNode(
                                                        new Token(TokenType.MINUS, new SingleLineTextRange(1, 5, 4, 1)),
                                                        UnaryOperator.MINUS),
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "b", new SingleLineTextRange(1, 6, 5, 1)))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 7, 6, 1)),
                                        new SingleLineTextRange(1, 1, 0, 7))),
                                new SingleLineTextRange(1, 1, 0, 7)),
                        new EndOfFileToken(new SingleLineTextRange(1, 8, 7, 0))),
                result.unit());
    }

    @Test
    public void negativeIntegerTest() {
        ParserOutput result = parse("x = -100;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new AssignmentStatementNode(
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "x", new SingleLineTextRange(1, 1, 0, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 2, 1, 1)))),
                                        new AssignmentOperatorNode(
                                                new Token(TokenType.EQUAL, new SingleLineTextRange(1, 3, 2, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 4, 3, 1))),
                                                AssignmentOperator.ASSIGNMENT,
                                                new SingleLineTextRange(1, 3, 2, 1)),
                                        new UnaryExpressionNode(
                                                new UnaryOperatorNode(
                                                        new Token(TokenType.MINUS, new SingleLineTextRange(1, 5, 4, 1)),
                                                        UnaryOperator.MINUS),
                                                new IntegerLiteralExpressionNode(
                                                        null,
                                                        new ValueToken(TokenType.INTEGER_LITERAL, "100", new SingleLineTextRange(1, 6, 5, 3)))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 9, 8, 1)),
                                        new SingleLineTextRange(1, 1, 0, 9))),
                                new SingleLineTextRange(1, 1, 0, 9)),
                        new EndOfFileToken(new SingleLineTextRange(1, 10, 9, 0))),
                result.unit());
    }

    @Test
    public void unaryMinusTest() {
        ParserOutput result = parse("x = -1 + 1;");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new AssignmentStatementNode(
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "x", new SingleLineTextRange(1, 1, 0, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 2, 1, 1)))),
                                        new AssignmentOperatorNode(
                                                new Token(TokenType.EQUAL, new SingleLineTextRange(1, 3, 2, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 4, 3, 1))),
                                                AssignmentOperator.ASSIGNMENT,
                                                new SingleLineTextRange(1, 3, 2, 1)),
                                        new BinaryExpressionNode(
                                                new UnaryExpressionNode(
                                                        new UnaryOperatorNode(
                                                                new Token(TokenType.MINUS, new SingleLineTextRange(1, 5, 4, 1)),
                                                                UnaryOperator.MINUS),
                                                        new IntegerLiteralExpressionNode(
                                                                null,
                                                                new ValueToken(TokenType.INTEGER_LITERAL, "1", new SingleLineTextRange(1, 6, 5, 1))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 7, 6, 1))))),
                                                new BinaryOperatorNode(
                                                        new Token(TokenType.PLUS, new SingleLineTextRange(1, 8, 7, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 9, 8, 1))),
                                                        BinaryOperator.PLUS),
                                                new IntegerLiteralExpressionNode(
                                                        null,
                                                        new ValueToken(TokenType.INTEGER_LITERAL, "1", new SingleLineTextRange(1, 10, 9, 1))),
                                                new SingleLineTextRange(1, 5, 4, 6)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 11, 10, 1)),
                                        new SingleLineTextRange(1, 1, 0, 11))),
                                new SingleLineTextRange(1, 1, 0, 11)),
                        new EndOfFileToken(new SingleLineTextRange(1, 12, 11, 0))),
                result.unit());
    }

    @Test
    public void endOfFileDiagnosticsTest() {
        ParserOutput result = parse("a()");
        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.SemicolonExpected, new SingleLineTextRange(1, 3, 2, 1))),
                result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 1, 0, 1))),
                                                new ArgumentsListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 2, 1, 1)),
                                                        SeparatedList.of(),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 3, 2, 1))),
                                                new SingleLineTextRange(1, 1, 0, 3)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 4, 3, 0)))),
                                new SingleLineTextRange(1, 1, 0, 3)),
                        new EndOfFileToken(new SingleLineTextRange(1, 4, 3, 0))),
                result.unit());
    }

    @Test
    public void leftAssociativityTest() {
        ParserOutput result = parse("""
                int x = 1 + 2 + 3;
                """);
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new VariableDeclarationNode(
                                        new PredefinedTypeNode(
                                                new Token(TokenType.INT, new SingleLineTextRange(1, 1, 0, 3))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 4, 3, 1))),
                                                PredefinedType.INT),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "x", new SingleLineTextRange(1, 5, 4, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 6, 5, 1)))),
                                        new Token(TokenType.EQUAL, new SingleLineTextRange(1, 7, 6, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 8, 7, 1))),
                                        new BinaryExpressionNode(
                                                new BinaryExpressionNode(
                                                        new IntegerLiteralExpressionNode(
                                                                null,
                                                                new ValueToken(TokenType.INTEGER_LITERAL, "1", new SingleLineTextRange(1, 9, 8, 1))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 10, 9, 1)))),
                                                        new BinaryOperatorNode(
                                                                new Token(TokenType.PLUS, new SingleLineTextRange(1, 11, 10, 1))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 12, 11, 1))),
                                                                BinaryOperator.PLUS),
                                                        new IntegerLiteralExpressionNode(
                                                                null,
                                                                new ValueToken(TokenType.INTEGER_LITERAL, "2", new SingleLineTextRange(1, 13, 12, 1))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 14, 13, 1)))),
                                                        new SingleLineTextRange(1, 9, 8, 5)),
                                                new BinaryOperatorNode(
                                                        new Token(TokenType.PLUS, new SingleLineTextRange(1, 15, 14, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 16, 15, 1))),
                                                        BinaryOperator.PLUS),
                                                new IntegerLiteralExpressionNode(
                                                        null,
                                                        new ValueToken(TokenType.INTEGER_LITERAL, "3", new SingleLineTextRange(1, 17, 16, 1))),
                                                new SingleLineTextRange(1, 9, 8, 9)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 18, 17, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 19, 2, 1, 18, 1))))),
                                new SingleLineTextRange(1, 1, 0, 18)),
                        new EndOfFileToken(new SingleLineTextRange(2, 1, 19, 0))),
                result.unit());
    }

    @Test
    public void letTest() {
        ParserOutput result = parse("""
                let x = 123;
                """);
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new VariableDeclarationNode(
                                        new LetTypeNode(
                                                new Token(TokenType.LET, new SingleLineTextRange(1, 1, 0, 3))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 4, 3, 1)))),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "x", new SingleLineTextRange(1, 5, 4, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 6, 5, 1)))),
                                        new Token(TokenType.EQUAL, new SingleLineTextRange(1, 7, 6, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 8, 7, 1))),
                                        new IntegerLiteralExpressionNode(
                                                null,
                                                new ValueToken(TokenType.INTEGER_LITERAL, "123", new SingleLineTextRange(1, 9, 8, 3))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 12, 11, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 13, 2, 1, 12, 1))))),
                                new SingleLineTextRange(1, 1, 0, 12)),
                        new EndOfFileToken(new SingleLineTextRange(2, 1, 13, 0))),
                result.unit());
    }

    @Test
    public void metaTest() {
        ParserOutput result = parse("""
                let x = #typeof(1) == #type(int);
                """);
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new VariableDeclarationNode(
                                        new LetTypeNode(
                                                new Token(TokenType.LET, new SingleLineTextRange(1, 1, 0, 3))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 4, 3, 1)))),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "x", new SingleLineTextRange(1, 5, 4, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 6, 5, 1)))),
                                        new Token(TokenType.EQUAL, new SingleLineTextRange(1, 7, 6, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 8, 7, 1))),
                                        new BinaryExpressionNode(
                                                new MetaTypeOfExpressionNode(
                                                        new Token(TokenType.META_TYPE_OF, new SingleLineTextRange(1, 9, 8, 7)),
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 16, 15, 1)),
                                                        new IntegerLiteralExpressionNode(
                                                                null,
                                                                new ValueToken(TokenType.INTEGER_LITERAL, "1", new SingleLineTextRange(1, 17, 16, 1))),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 18, 17, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 19, 18, 1)))),
                                                new BinaryOperatorNode(
                                                        new Token(TokenType.EQUAL_EQUAL, new SingleLineTextRange(1, 20, 19, 2))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 22, 21, 1))),
                                                        BinaryOperator.EQUALS),
                                                new MetaTypeExpressionNode(
                                                        new Token(TokenType.META_TYPE, new SingleLineTextRange(1, 23, 22, 5)),
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 28, 27, 1)),
                                                        new PredefinedTypeNode(
                                                                new Token(TokenType.INT, new SingleLineTextRange(1, 29, 28, 3)),
                                                                PredefinedType.INT),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 32, 31, 1))),
                                                new SingleLineTextRange(1, 9, 8, 24)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 33, 32, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 34, 2, 1, 33, 1))))),
                                new SingleLineTextRange(1, 1, 0, 33)),
                        new EndOfFileToken(new SingleLineTextRange(2, 1, 34, 0))),
                result.unit());
    }

    @Test
    public void javaRawTypeTest() {
        ParserOutput result = parse("""
                Java<com.example.ClassA> a;
                """);
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new VariableDeclarationNode(
                                        new JavaTypeNode(
                                                new ValueToken(TokenType.IDENTIFIER, "Java", new SingleLineTextRange(1, 1, 0, 4)),
                                                new Token(TokenType.LESS, new SingleLineTextRange(1, 5, 4, 1)),
                                                new JavaQualifiedTypeNameNode(List.of(
                                                        new ValueToken(TokenType.IDENTIFIER, "com", new SingleLineTextRange(1, 6, 5, 3)),
                                                        new Token(TokenType.DOT, new SingleLineTextRange(1, 9, 8, 1)),
                                                        new ValueToken(TokenType.IDENTIFIER, "example", new SingleLineTextRange(1, 10, 9, 7)),
                                                        new Token(TokenType.DOT, new SingleLineTextRange(1, 17, 16, 1)),
                                                        new ValueToken(TokenType.IDENTIFIER, "ClassA", new SingleLineTextRange(1, 18, 17, 6))),
                                                        "com.example.ClassA"),
                                                new Token(TokenType.GREATER, new SingleLineTextRange(1, 24, 23, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 25, 24, 1)))),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 26, 25, 1))),
                                        null,
                                        null,
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 27, 26, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 28, 2, 1, 27, 1))))),
                                new SingleLineTextRange(1, 1, 0, 27)),
                        new EndOfFileToken(new SingleLineTextRange(2, 1, 28, 0))),
                result.unit());
    }

    @Test
    public void customTypeTest() {
        ParserOutput result = parse("""
                CustomType a;
                """);
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new VariableDeclarationNode(
                                        new CustomTypeNode(
                                                new ValueToken(TokenType.IDENTIFIER, "CustomType", new SingleLineTextRange(1, 1, 0, 10))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 11, 10, 1)))),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 12, 11, 1))),
                                        null,
                                        null,
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 13, 12, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 14, 2, 1, 13, 1))))),
                                new SingleLineTextRange(1, 1, 0, 13)),
                        new EndOfFileToken(new SingleLineTextRange(2, 1, 14, 0))),
                result.unit());
    }

    @Test
    public void notAStatementTest1() {
        ParserOutput result = parse("""
                a.b;
                """);
        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.NotAStatement, new SingleLineTextRange(1, 1, 0, 3))),
                result.diagnostics());
    }

    @Test
    public void notAStatementTest2() {
        ParserOutput result = parse("""
                a + b;
                """);
        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.NotAStatement, new SingleLineTextRange(1, 1, 0, 5))),
                result.diagnostics());
    }

    @Test
    public void notAStatementTest3() {
        ParserOutput result = parse("""
                "ab";
                """);
        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.NotAStatement, new SingleLineTextRange(1, 1, 0, 4))),
                result.diagnostics());
    }

    @Test
    public void notAStatementTest4() {
        ParserOutput result = parse("""
                (a == b);
                """);
        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.NotAStatement, new SingleLineTextRange(1, 1, 0, 8))),
                result.diagnostics());
    }
}