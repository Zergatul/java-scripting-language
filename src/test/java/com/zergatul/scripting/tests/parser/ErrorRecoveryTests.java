package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.ParserErrors;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.PredefinedType;
import com.zergatul.scripting.parser.nodes.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ErrorRecoveryTests extends ParserTestBase {

    @Test
    public void unfinishedMemberAccess1Test() {
        ParserOutput result = parse("""
                freeCam.t
                freeCam.toggle();
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.NotAStatement, new SingleLineTextRange(1, 1, 0, 9)),
                new DiagnosticMessage(ParserErrors.SemicolonExpected, new SingleLineTextRange(1, 9, 8, 1))),
                result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ExpressionStatementNode(
                                        new MemberAccessExpressionNode(
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "freeCam", new SingleLineTextRange(1, 1, 0, 7))),
                                                new Token(TokenType.DOT, new SingleLineTextRange(1, 8, 7, 1)),
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "t", new SingleLineTextRange(1, 9, 8, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 10, 2, 1, 9, 1))))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 10, 9, 0))),
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new MemberAccessExpressionNode(
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "freeCam", new SingleLineTextRange(2, 1, 10, 7))),
                                                        new Token(TokenType.DOT, new SingleLineTextRange(2, 8, 17, 1)),
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "toggle", new SingleLineTextRange(2, 9, 18, 6)))),
                                                new ArgumentsListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 15, 24, 1)),
                                                        SeparatedList.of(),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 16, 25, 1))),
                                                new SingleLineTextRange(2, 1, 10, 16)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(2, 17, 26, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(2, 18, 3, 1, 27, 1))))),
                                new MultiLineTextRange(1, 1, 2, 18, 0, 27)),
                        new EndOfFileToken(new SingleLineTextRange(3, 1, 28, 0))),
                result.unit());
    }

    @Test
    public void unfinishedMemberAccess2Test() {
        ParserOutput result = parse("""
                obj.
                boolean bbb = true;
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.IdentifierExpected, new SingleLineTextRange(2, 1, 5, 7), "boolean"),
                new DiagnosticMessage(ParserErrors.NotAStatement, new SingleLineTextRange(1, 1, 0, 4)),
                new DiagnosticMessage(ParserErrors.SemicolonExpected, new SingleLineTextRange(1, 4, 3, 1))),
                result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ExpressionStatementNode(
                                        new MemberAccessExpressionNode(
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "obj", new SingleLineTextRange(1, 1, 0, 3))),
                                                new Token(TokenType.DOT, new SingleLineTextRange(1, 4, 3, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 5, 2, 1, 4, 1))),
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "", new SingleLineTextRange(1, 5, 4, 0)))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 5, 4, 0))),
                                new VariableDeclarationNode(
                                        new PredefinedTypeNode(
                                                new Token(TokenType.BOOLEAN, new SingleLineTextRange(2, 1, 5, 7))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 8, 12, 1))),
                                                PredefinedType.BOOLEAN),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "bbb", new SingleLineTextRange(2, 9, 13, 3))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 12, 16, 1)))),
                                        new Token(TokenType.EQUAL, new SingleLineTextRange(2, 13, 17, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 14, 18, 1))),
                                        new BooleanLiteralExpressionNode(
                                                new Token(TokenType.TRUE, new SingleLineTextRange(2, 15, 19, 4)),
                                                true),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(2, 19, 23, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(2, 20, 3, 1, 24, 1))))),
                                new MultiLineTextRange(1, 1, 2, 20, 0, 24)),
                        new EndOfFileToken(new SingleLineTextRange(3, 1, 25, 0))),
                result.unit());
    }

    @Test
    public void notClosedBlockTest() {
        ParserOutput result = parse("""
                { *
                """);
        Assertions.assertFalse(result.diagnostics().isEmpty());
    }

    @Test
    public void missingArgumentTest() {
        ParserOutput result = parse("""
                main.chat("abc",);
                """);
        Assertions.assertFalse(result.diagnostics().isEmpty());
    }

    @Test
    public void missingColonTest() {
        ParserOutput result = parse("""
                2 > 1 ? 3
                """);
        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.ColonExpected, new SingleLineTextRange(1, 9, 8, 1))),
                result.diagnostics().stream().limit(1).toList());
    }

    @Test
    public void unfinishedIfStatementTest() {
        ParserOutput result = parse("""
                if (game.)
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.IdentifierExpected, new SingleLineTextRange(1, 10, 9, 1), ")"),
                new DiagnosticMessage(ParserErrors.StatementExpected, new SingleLineTextRange(2, 1, 11, 0), "<EOF>")),
                result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new IfStatementNode(
                                        new Token(TokenType.IF, new SingleLineTextRange(1, 1, 0, 2))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 3, 2, 1))),
                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 4, 3, 1)),
                                        new MemberAccessExpressionNode(
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "game", new SingleLineTextRange(1, 5, 4, 4))),
                                                new Token(TokenType.DOT, new SingleLineTextRange(1, 9, 8, 1)),
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "", new SingleLineTextRange(1, 10, 9, 0)))),
                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 10, 9, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 11, 2, 1, 10, 1))),
                                        new InvalidStatementNode(new SingleLineTextRange(1, 11, 10, 0)),
                                        null,
                                        null,
                                        new SingleLineTextRange(1, 1, 0, 10))),
                                new SingleLineTextRange(1, 1, 0, 10)),
                        new EndOfFileToken(new SingleLineTextRange(2, 1, 11, 0))),
                result.unit());
    }

    @Test
    public void unfinishedBinaryExpressionTest() {
        ParserOutput result = parse("""
                return a > b || ;
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.ExpressionExpected, new SingleLineTextRange(1, 17, 16, 1), ";")),
                result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ReturnStatementNode(
                                        new Token(TokenType.RETURN, new SingleLineTextRange(1, 1, 0, 6))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 7, 6, 1))),
                                        new BinaryExpressionNode(
                                                new BinaryExpressionNode(
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 8, 7, 1))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 9, 8, 1)))),
                                                        new BinaryOperatorNode(
                                                                new Token(TokenType.GREATER, new SingleLineTextRange(1, 10, 9, 1))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 11, 10, 1))),
                                                                BinaryOperator.GREATER),
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "b", new SingleLineTextRange(1, 12, 11, 1))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 13, 12, 1)))),
                                                        new SingleLineTextRange(1, 8, 7, 5)),
                                                new BinaryOperatorNode(
                                                        new Token(TokenType.PIPE_PIPE, new SingleLineTextRange(1, 14, 13, 2))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 16, 15, 1))),
                                                        BinaryOperator.BOOLEAN_OR),
                                                new InvalidExpressionNode(new SingleLineTextRange(1, 17, 16, 0)),
                                                new SingleLineTextRange(1, 8, 7, 9)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 17, 16, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 18, 2, 1, 17, 1))))),
                                new SingleLineTextRange(1, 1, 0, 17)),
                        new EndOfFileToken(new SingleLineTextRange(2, 1, 18, 0))),
                result.unit());
    }

    @Test
    public void unfinishedArrayDeclarationTest() {
        ParserOutput result = parse("""
                int[] x = new int[] { 1 ;
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.CommaOrCloseCurlyBracketExpected, new SingleLineTextRange(1, 25, 24, 1)),
                new DiagnosticMessage(ParserErrors.CloseCurlyBracketExpected, new SingleLineTextRange(1, 25, 24, 1), ";")),
                result.diagnostics());

        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new VariableDeclarationNode(
                                        new ArrayTypeNode(
                                                new PredefinedTypeNode(
                                                        new Token(TokenType.INT, new SingleLineTextRange(1, 1, 0, 3)),
                                                        PredefinedType.INT),
                                                new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(1, 4, 3, 1)),
                                                new Token(TokenType.RIGHT_SQUARE_BRACKET, new SingleLineTextRange(1, 5, 4, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 6, 5, 1))),
                                                new SingleLineTextRange(1, 1, 0, 5)),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "x", new SingleLineTextRange(1, 7, 6, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 8, 7, 1)))),
                                        new Token(TokenType.EQUAL, new SingleLineTextRange(1, 9, 8, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 10, 9, 1))),
                                        new ArrayInitializerExpressionNode(
                                                new Token(TokenType.NEW, new SingleLineTextRange(1, 11, 10, 3))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 14, 13, 1))),
                                                new ArrayTypeNode(
                                                        new PredefinedTypeNode(
                                                                new Token(TokenType.INT, new SingleLineTextRange(1, 15, 14, 3)),
                                                                PredefinedType.INT),
                                                        new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(1, 18, 17, 1)),
                                                        new Token(TokenType.RIGHT_SQUARE_BRACKET, new SingleLineTextRange(1, 19, 18, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 20, 19, 1))),
                                                        new SingleLineTextRange(1, 15, 14, 5)),
                                                new Token(TokenType.LEFT_CURLY_BRACKET, new SingleLineTextRange(1, 21, 20, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 22, 21, 1))),
                                                SeparatedList.of(
                                                        ExpressionNode.class,
                                                        new IntegerLiteralExpressionNode(
                                                                null,
                                                                new ValueToken(TokenType.INTEGER_LITERAL, "1", new SingleLineTextRange(1, 23, 22, 1))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 24, 23, 1))))),
                                                new Token(TokenType.RIGHT_CURLY_BRACKET, new SingleLineTextRange(1, 25, 24, 0)),
                                                new SingleLineTextRange(1, 11, 10, 13)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 25, 24, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 26, 2, 1, 25, 1))))),
                                new SingleLineTextRange(1, 1, 0, 25)),
                        new EndOfFileToken(new SingleLineTextRange(2, 1, 26, 0))),
                result.unit());
    }

    @Test
    public void unfinishedStatement1Test() {
        ParserOutput result = parse("""
                f
                freeCam.toggle();
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.NotAStatement, new SingleLineTextRange(1, 1, 0, 1)),
                new DiagnosticMessage(ParserErrors.SemicolonExpected, new SingleLineTextRange(1, 1, 0, 1))),
                result.diagnostics());

        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ExpressionStatementNode(
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "f", new SingleLineTextRange(1, 1, 0, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 2, 2, 1, 1, 1)))),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 2, 1, 0))),
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new MemberAccessExpressionNode(
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "freeCam", new SingleLineTextRange(2, 1, 2, 7))),
                                                        new Token(TokenType.DOT, new SingleLineTextRange(2, 8, 9, 1)),
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "toggle", new SingleLineTextRange(2, 9, 10, 6)))),
                                                new ArgumentsListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 15, 16, 1)),
                                                        SeparatedList.of(),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 16, 17, 1))),
                                                new SingleLineTextRange(2, 1, 2, 16)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(2, 17, 18, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(2, 18, 3, 1, 19, 1))))),
                                new MultiLineTextRange(1, 1, 2, 18, 0, 19)),
                        new EndOfFileToken(new SingleLineTextRange(3, 1, 20, 0))),
                result.unit());
    }

    @Test
    public void unfinishedStatement2Test() {
        ParserOutput result = parse("""
                int
                freeCam.toggle();
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.IdentifierExpected, new SingleLineTextRange(2, 1, 4, 7), "freeCam.")),
                result.diagnostics());

        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new VariableDeclarationNode(
                                        new PredefinedTypeNode(
                                                new Token(TokenType.INT, new SingleLineTextRange(1, 1, 0, 3))
                                                        .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 4, 2, 1, 3, 1))),
                                                PredefinedType.INT),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "", new SingleLineTextRange(2, 1, 4, 0))),
                                        null,
                                        null,
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(2, 1, 4, 0))),
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new MemberAccessExpressionNode(
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "freeCam", new SingleLineTextRange(2, 1, 4, 7))),
                                                        new Token(TokenType.DOT, new SingleLineTextRange(2, 8, 11, 1)),
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "toggle", new SingleLineTextRange(2, 9, 12, 6)))),
                                                new ArgumentsListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 15, 18, 1)),
                                                        SeparatedList.of(),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 16, 19, 1))),
                                                new SingleLineTextRange(2, 1, 4, 16)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(2, 17, 20, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(2, 18, 3, 1, 21, 1))))),
                                new MultiLineTextRange(1, 1, 2, 18, 0, 21)),
                        new EndOfFileToken(new SingleLineTextRange(3, 1, 22, 0))),
                result.unit());
    }

    @Test
    public void unfinishedStatement3Test() {
        ParserOutput result = parse("""
                if
                freeCam.toggle();
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.LeftParenthesisExpected, new SingleLineTextRange(2, 1, 3, 7), "freeCam")),
                result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new IfStatementNode(
                                        new Token(TokenType.IF, new SingleLineTextRange(1, 1, 0, 2))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 3, 2, 1, 2, 1))),
                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 3, 2, 0)),
                                        new InvalidExpressionNode(new SingleLineTextRange(2, 1, 3, 0)),
                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 1, 3, 0)),
                                        new InvalidStatementNode(new SingleLineTextRange(2, 1, 3, 0)),
                                        null,
                                        null,
                                        new SingleLineTextRange(1, 1, 0, 2)),
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new MemberAccessExpressionNode(
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "freeCam", new SingleLineTextRange(2, 1, 3, 7))),
                                                        new Token(TokenType.DOT, new SingleLineTextRange(2, 8, 10, 1)),
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "toggle", new SingleLineTextRange(2, 9, 11, 6)))),
                                                new ArgumentsListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 15, 17, 1)),
                                                        SeparatedList.of(),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 16, 18, 1))),
                                                new SingleLineTextRange(2, 1, 3, 16)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(2, 17, 19, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(2, 18, 3, 1, 20, 1))))),
                                new MultiLineTextRange(1, 1, 2, 18, 0, 20)),
                        new EndOfFileToken(new SingleLineTextRange(3, 1, 21, 0))),
                result.unit());
    }

    @Test
    public void unfinishedStatement4Test() {
        ParserOutput result = parse("""
                for
                freeCam.toggle();
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.LeftParenthesisExpected, new SingleLineTextRange(2, 1, 4, 7), "freeCam")),
                result.diagnostics());

        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ForLoopStatementNode(
                                        new Token(TokenType.FOR, new SingleLineTextRange(1, 1, 0, 3))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 4, 2, 1, 3, 1))),
                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 4, 3, 0)),
                                        new InvalidStatementNode(new SingleLineTextRange(1, 4, 3, 0)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 4, 3, 0)),
                                        new InvalidExpressionNode(new SingleLineTextRange(1, 4, 3, 0)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 4, 3, 0)),
                                        new InvalidStatementNode(new SingleLineTextRange(1, 4, 3, 0)),
                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 4, 3, 0)),
                                        new InvalidStatementNode(new SingleLineTextRange(1, 4, 3, 0))),
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new MemberAccessExpressionNode(
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "freeCam", new SingleLineTextRange(2, 1, 4, 7))),
                                                        new Token(TokenType.DOT, new SingleLineTextRange(2, 8, 11, 1)),
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "toggle", new SingleLineTextRange(2, 9, 12, 6)))),
                                                new ArgumentsListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 15, 18, 1)),
                                                        SeparatedList.of(),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 16, 19, 1))),
                                                new SingleLineTextRange(2, 1, 4, 16)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(2, 17, 20, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(2, 18, 3, 1, 21, 1))))),
                                new MultiLineTextRange(1, 1, 2, 18, 0, 21)),
                        new EndOfFileToken(new SingleLineTextRange(3, 1, 22, 0))),
                result.unit());
    }

    @Test
    public void unfinishedStatement5Test() {
        ParserOutput result = parse("""
                foreach
                freeCam.toggle();
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.LeftParenthesisExpected, new SingleLineTextRange(2, 1, 8, 7), "freeCam")),
                result.diagnostics());

        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ForEachLoopStatementNode(
                                        new Token(TokenType.FOREACH, new SingleLineTextRange(1, 1, 0, 7))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 8, 2, 1, 7, 1))),
                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 8, 7, 0)),
                                        new InvalidTypeNode(
                                                new ValueToken(TokenType.IDENTIFIER, "", new SingleLineTextRange(1, 8, 7, 0))),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "", new SingleLineTextRange(1, 8, 7, 0))),
                                        new Token(TokenType.IN, new SingleLineTextRange(1, 8, 7, 0)),
                                        new InvalidExpressionNode(new SingleLineTextRange(1, 8, 7, 0)),
                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 8, 7, 0)),
                                        new InvalidStatementNode(new SingleLineTextRange(1, 8, 7, 0))),
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new MemberAccessExpressionNode(
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "freeCam", new SingleLineTextRange(2, 1, 8, 7))),
                                                        new Token(TokenType.DOT, new SingleLineTextRange(2, 8, 15, 1)),
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "toggle", new SingleLineTextRange(2, 9, 16, 6)))),
                                                new ArgumentsListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 15, 22, 1)),
                                                        SeparatedList.of(),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 16, 23, 1))),
                                                new SingleLineTextRange(2, 1, 8, 16)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(2, 17, 24, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(2, 18, 3, 1, 25, 1))))),
                                new MultiLineTextRange(1, 1, 2, 18, 0, 25)),
                        new EndOfFileToken(new SingleLineTextRange(3, 1, 26, 0))),
                result.unit());
    }

    @Test
    public void unfinishedStatement6Test() {
        ParserOutput result = parse("""
                while
                freeCam.toggle();
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.LeftParenthesisExpected, new SingleLineTextRange(2, 1, 6, 7), "freeCam")),
                result.diagnostics());

        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new WhileLoopStatementNode(
                                        new Token(TokenType.WHILE, new SingleLineTextRange(1, 1, 0, 5))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 6, 2, 1, 5, 1))),
                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 6, 5, 0)),
                                        new InvalidExpressionNode(new SingleLineTextRange(2, 1, 6, 0)),
                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 1, 6, 0)),
                                        new InvalidStatementNode(new SingleLineTextRange(2, 1, 6, 0))),
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new MemberAccessExpressionNode(
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "freeCam", new SingleLineTextRange(2, 1, 6, 7))),
                                                        new Token(TokenType.DOT, new SingleLineTextRange(2, 8, 13, 1)),
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "toggle", new SingleLineTextRange(2, 9, 14, 6)))),
                                                new ArgumentsListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 15, 20, 1)),
                                                        SeparatedList.of(),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 16, 21, 1))),
                                                new SingleLineTextRange(2, 1, 6, 16)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(2, 17, 22, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(2, 18, 3, 1, 23, 1))))),
                                new MultiLineTextRange(1, 1, 2, 18, 0, 23)),
                        new EndOfFileToken(new SingleLineTextRange(3, 1, 24, 0))),
                result.unit());
    }

    @Test
    public void unfinishedFunctionTest() {
        ParserOutput result = parse("""
                void abc
                freeCam.toggle();
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.LeftParenthesisExpected, new SingleLineTextRange(2, 1, 9, 7), "freeCam")),
                result.diagnostics());

        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(
                                new FunctionNode(
                                        new ModifiersNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                                        new VoidTypeNode(
                                                new Token(TokenType.VOID, new SingleLineTextRange(1, 1, 0, 4))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 5, 4, 1)))),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "abc", new SingleLineTextRange(1, 6, 5, 3))
                                                        .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 9, 2, 1, 8, 1)))),
                                        new ParameterListNode(
                                                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 9, 8, 0)),
                                                SeparatedList.of(),
                                                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 9, 8, 0))),
                                        null,
                                        new BlockStatementNode(
                                                new Token(TokenType.LEFT_CURLY_BRACKET, new SingleLineTextRange(1, 9, 8, 0)),
                                                List.of(),
                                                new Token(TokenType.RIGHT_CURLY_BRACKET, new SingleLineTextRange(1, 9, 8, 0))))),
                                new SingleLineTextRange(1, 1, 0, 8)),
                        new StatementsListNode(List.of(
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new MemberAccessExpressionNode(
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "freeCam", new SingleLineTextRange(2, 1, 9, 7))),
                                                        new Token(TokenType.DOT, new SingleLineTextRange(2, 8, 16, 1)),
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "toggle", new SingleLineTextRange(2, 9, 17, 6)))),
                                                new ArgumentsListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 15, 23, 1)),
                                                        SeparatedList.of(),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 16, 24, 1))),
                                                new SingleLineTextRange(2, 1, 9, 16)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(2, 17, 25, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(2, 18, 3, 1, 26, 1))))),
                                new SingleLineTextRange(2, 1, 9, 17)),
                        new EndOfFileToken(new SingleLineTextRange(3, 1, 27, 0))),
                result.unit());
    }

    @Test
    public void unfinishedParametersTest() {
        ParserOutput result = parse("""
                obj.method(100,)
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.ExpressionExpected, new SingleLineTextRange(1, 16, 15, 1), ")"),
                new DiagnosticMessage(ParserErrors.SemicolonExpected, new SingleLineTextRange(1,16, 15, 1))),
                result.diagnostics());

        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new MemberAccessExpressionNode(
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "obj", new SingleLineTextRange(1, 1, 0, 3))),
                                                        new Token(TokenType.DOT, new SingleLineTextRange(1, 4, 3, 1)),
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "method", new SingleLineTextRange(1, 5, 4, 6)))),
                                                new ArgumentsListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 11, 10, 1)),
                                                        SeparatedList.of(
                                                                ExpressionNode.class,
                                                                new IntegerLiteralExpressionNode(
                                                                        null,
                                                                        new ValueToken(TokenType.INTEGER_LITERAL, "100", new SingleLineTextRange(1, 12, 11, 3))),
                                                                new Token(TokenType.COMMA, new SingleLineTextRange(1, 15, 14, 1))),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 16, 15, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 17, 2, 1, 16, 1)))),
                                                new SingleLineTextRange(1, 1, 0, 16)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 17, 16, 0)))),
                                new SingleLineTextRange(1, 1, 0, 16)),
                        new EndOfFileToken(new SingleLineTextRange(2, 1, 17, 0))),
                result.unit());
    }
}