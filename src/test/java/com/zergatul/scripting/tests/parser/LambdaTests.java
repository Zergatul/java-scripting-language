package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.AssignmentOperator;
import com.zergatul.scripting.parser.ParserErrors;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.PredefinedType;
import com.zergatul.scripting.parser.nodes.*;
import com.zergatul.scripting.tests.utility.MarkedCode;
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

    @Test
    public void lambdaRecoveryTest1() {
        MarkedCode marked = MarkedCode.from("""
                int x = 1;
                fn<int => int> mapper = value => ⟦f⟧
                x = 2;
                """);

        ParserOutput result = parse(marked.getCode());

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(ParserErrors.SemicolonExpected, marked.getRange("⟦⟧"))),
                result.diagnostics());

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
                                                new ValueToken(TokenType.INTEGER_LITERAL, "1", new SingleLineTextRange(1, 9, 8, 1))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 10, 9, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 11, 2, 1, 10, 1)))),
                                new VariableDeclarationNode(
                                        new FunctionTypeNode(
                                                new ValueToken(TokenType.IDENTIFIER, "fn", new SingleLineTextRange(2, 1, 11, 2)),
                                                new Token(TokenType.LESS, new SingleLineTextRange(2, 3, 13, 1)),
                                                null,
                                                SeparatedList.of(
                                                        TypeNode.class,
                                                        new PredefinedTypeNode(
                                                                new Token(TokenType.INT, new SingleLineTextRange(2, 4, 14, 3))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 7, 17, 1))),
                                                                PredefinedType.INT)),
                                                null,
                                                new Token(TokenType.EQUAL_GREATER, new SingleLineTextRange(2, 8, 18, 2))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 10, 20, 1))),
                                                new PredefinedTypeNode(
                                                        new Token(TokenType.INT, new SingleLineTextRange(2, 11, 21, 3)),
                                                        PredefinedType.INT),
                                                new Token(TokenType.GREATER, new SingleLineTextRange(2, 14, 24, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 15, 25, 1)))),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "mapper", new SingleLineTextRange(2, 16, 26, 6))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 22, 32, 1)))),
                                        new Token(TokenType.EQUAL, new SingleLineTextRange(2, 23, 33, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 24, 34, 1))),
                                        new LambdaExpressionNode(
                                                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 25, 35, 0)),
                                                SeparatedList.of(
                                                        NameExpressionNode.class,
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "value", new SingleLineTextRange(2, 25, 35, 5))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 30, 40, 1))))),
                                                new ValueToken(TokenType.RIGHT_PARENTHESES, "", new SingleLineTextRange(2, 30, 40, 0)),
                                                new Token(TokenType.EQUAL_GREATER, new SingleLineTextRange(2, 31, 41, 2))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 33, 43, 1))),
                                                new ExpressionStatementNode(
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "f", new SingleLineTextRange(2, 34, 44, 1))
                                                                        .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(2, 35, 3, 1, 45, 1)))),
                                                        null)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(2, 35, 45, 0))),
                                new AssignmentStatementNode(
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "x", new SingleLineTextRange(3, 1, 46, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(3, 2, 47, 1)))),
                                        new AssignmentOperatorNode(
                                                new Token(TokenType.EQUAL, new SingleLineTextRange(3, 3, 48, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(3, 4, 49, 1))),
                                                AssignmentOperator.ASSIGNMENT,
                                                new SingleLineTextRange(3, 3, 48, 1)),
                                        new IntegerLiteralExpressionNode(
                                                null,
                                                new ValueToken(TokenType.INTEGER_LITERAL, "2", new SingleLineTextRange(3, 5, 50, 1))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(3, 6, 51, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(3, 7, 4, 1, 52, 1))),
                                        new SingleLineTextRange(3, 1, 46, 6))),
                                new MultiLineTextRange(1, 1, 3, 7, 0, 52)),
                        new EndOfFileToken(new SingleLineTextRange(4, 1, 53, 0))),
                result.unit());
    }

    @Test
    public void lambdaRecoveryTest2() {
        MarkedCode marked = MarkedCode.from("""
                int x = 1;
                fn<int => int> mapper = value ⟪=>⟫ ⟦for⟧
                ❰x❱ = 2;
                """);

        ParserOutput result = parse(marked.getCode());

        comparator.assertEquals(
                List.of(
                        new DiagnosticMessage(ParserErrors.SimpleStatementExpected, marked.getRange("⟦⟧"), "for"),
                        new DiagnosticMessage(ParserErrors.SemicolonExpected, marked.getRange("⟪⟫")),
                        new DiagnosticMessage(ParserErrors.LeftParenthesisExpected, marked.getRange("❰❱"), "x")),
                result.diagnostics());

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
                                                new ValueToken(TokenType.INTEGER_LITERAL, "1", new SingleLineTextRange(1, 9, 8, 1))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 10, 9, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 11, 2, 1, 10, 1)))),
                                new VariableDeclarationNode(
                                        new FunctionTypeNode(
                                                new ValueToken(TokenType.IDENTIFIER, "fn", new SingleLineTextRange(2, 1, 11, 2)),
                                                new Token(TokenType.LESS, new SingleLineTextRange(2, 3, 13, 1)),
                                                null,
                                                SeparatedList.of(
                                                        TypeNode.class,
                                                        new PredefinedTypeNode(
                                                                new Token(TokenType.INT, new SingleLineTextRange(2, 4, 14, 3))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 7, 17, 1))),
                                                                PredefinedType.INT)),
                                                null,
                                                new Token(TokenType.EQUAL_GREATER, new SingleLineTextRange(2, 8, 18, 2))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 10, 20, 1))),
                                                new PredefinedTypeNode(
                                                        new Token(TokenType.INT, new SingleLineTextRange(2, 11, 21, 3)),
                                                        PredefinedType.INT),
                                                new Token(TokenType.GREATER, new SingleLineTextRange(2, 14, 24, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 15, 25, 1)))),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "mapper", new SingleLineTextRange(2, 16, 26, 6))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 22, 32, 1)))),
                                        new Token(TokenType.EQUAL, new SingleLineTextRange(2, 23, 33, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 24, 34, 1))),
                                        new LambdaExpressionNode(
                                                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 25, 35, 0)),
                                                SeparatedList.of(
                                                        NameExpressionNode.class,
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "value", new SingleLineTextRange(2, 25, 35, 5))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 30, 40, 1))))),
                                                new ValueToken(TokenType.RIGHT_PARENTHESES, "", new SingleLineTextRange(2, 30, 40, 0)),
                                                new Token(TokenType.EQUAL_GREATER, new SingleLineTextRange(2, 31, 41, 2))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 33, 43, 1))),
                                                new InvalidStatementNode(new SingleLineTextRange(2, 34, 44, 0))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(2, 33, 43, 0))),
                                new ForLoopStatementNode(
                                        new Token(TokenType.FOR, new SingleLineTextRange(2, 34, 44, 3))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(2, 37, 3, 1, 47, 1))),
                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 37, 47, 0)),
                                        new InvalidStatementNode(new SingleLineTextRange(2, 37, 47, 0)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(2, 37, 47, 0)),
                                        new InvalidExpressionNode(new SingleLineTextRange(2, 37, 47, 0)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(2, 37, 47, 0)),
                                        new InvalidStatementNode(new SingleLineTextRange(2, 37, 47, 0)),
                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 37, 47, 0)),
                                        new InvalidStatementNode(new SingleLineTextRange(2, 37, 47, 0))),
                                new AssignmentStatementNode(
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "x", new SingleLineTextRange(3, 1, 48, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(3, 2, 49, 1)))),
                                        new AssignmentOperatorNode(
                                                new Token(TokenType.EQUAL, new SingleLineTextRange(3, 3, 50, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(3, 4, 51, 1))),
                                                AssignmentOperator.ASSIGNMENT,
                                                new SingleLineTextRange(3, 3, 50, 1)),
                                        new IntegerLiteralExpressionNode(
                                                null,
                                                new ValueToken(TokenType.INTEGER_LITERAL, "2", new SingleLineTextRange(3, 5, 52, 1))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(3, 6, 53, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(3, 7, 4, 1, 54, 1))),
                                        new SingleLineTextRange(3, 1, 48, 6))),
                                new MultiLineTextRange(1, 1, 3, 7, 0, 54)),
                        new EndOfFileToken(new SingleLineTextRange(4, 1, 55, 0))),
                result.unit());
    }
}