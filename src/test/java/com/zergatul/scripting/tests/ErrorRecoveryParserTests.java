package com.zergatul.scripting.tests;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.parser.*;
import com.zergatul.scripting.parser.nodes.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ErrorRecoveryParserTests {

    @Test
    public void unfinishedMemberAccess1Test() {
        ParserOutput result = parse("""
                freeCam.t
                freeCam.toggle();
                """);

        Assertions.assertIterableEquals(
                result.diagnostics(),
                List.of(
                        new DiagnosticMessage(ParserErrors.SemicolonExpected, new SingleLineTextRange(1, 9, 8, 1))));

        Assertions.assertIterableEquals(
                result.unit().statements.statements,
                List.of(
                        new ExpressionStatementNode(
                                new MemberAccessExpressionNode(
                                        new NameExpressionNode("freeCam", new SingleLineTextRange(1, 1, 0, 7)),
                                        new NameExpressionNode("t", new SingleLineTextRange(1, 9, 8, 1)),
                                        new SingleLineTextRange(1, 1, 0, 9)),
                                new SingleLineTextRange(1, 1, 0, 9)),
                        new ExpressionStatementNode(
                                new InvocationExpressionNode(
                                        new MemberAccessExpressionNode(
                                            new NameExpressionNode("freeCam", new SingleLineTextRange(2, 1, 10, 7)),
                                            new NameExpressionNode("toggle", new SingleLineTextRange(2, 9, 18, 6)),
                                            new SingleLineTextRange(2, 1, 10, 14)),
                                        new ArgumentsListNode(List.of(), new SingleLineTextRange(2, 15, 24, 2)),
                                        new SingleLineTextRange(2, 1, 10, 16)),
                                new SingleLineTextRange(2, 1, 10, 17))));
    }

    @Test
    public void unfinishedMemberAccess2Test() {
        ParserOutput result = parse("""
                obj.
                boolean bbb = true;
                """);

        Assertions.assertIterableEquals(
                result.diagnostics(),
                List.of(
                        new DiagnosticMessage(ParserErrors.IdentifierExpected, new SingleLineTextRange(2, 1, 5, 7), "boolean"),
                        new DiagnosticMessage(ParserErrors.SemicolonExpected, new SingleLineTextRange(1, 4, 3, 1))));

        Assertions.assertIterableEquals(
                result.unit().statements.statements,
                List.of(
                        new ExpressionStatementNode(
                                new MemberAccessExpressionNode(
                                        new NameExpressionNode("obj", new SingleLineTextRange(1, 1, 0, 3)),
                                        new NameExpressionNode("", new SingleLineTextRange(1, 5, 4, 0)),
                                        new SingleLineTextRange(1, 1, 0, 4)),
                                new SingleLineTextRange(1, 1, 0, 4)),
                        new VariableDeclarationNode(
                                new PredefinedTypeNode(PredefinedType.BOOLEAN, new SingleLineTextRange(2, 1, 5, 7)),
                                new NameExpressionNode("bbb", new SingleLineTextRange(2, 9, 13, 3)),
                                new BooleanLiteralExpressionNode(true, new SingleLineTextRange(2, 15, 19, 4)),
                                new SingleLineTextRange(2, 1, 5, 19))));
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
        Assertions.assertFalse(result.diagnostics().isEmpty());
        Assertions.assertEquals(
                result.diagnostics().get(0),
                new DiagnosticMessage(ParserErrors.ColonExpected, new SingleLineTextRange(1, 9, 8, 1)));
    }

    @Test
    public void unfinishedIfStatementTest() {
        ParserOutput result = parse("""
                if (game.)
                """);

        Assertions.assertIterableEquals(
                result.diagnostics(),
                List.of(
                        new DiagnosticMessage(ParserErrors.IdentifierExpected, new SingleLineTextRange(1, 10, 9, 1), ")"),
                        new DiagnosticMessage(ParserErrors.StatementExpected, new SingleLineTextRange(1, 12, 11, 1), "<EOF>")));

        Assertions.assertEquals(
                result.unit().statements,
                new StatementsListNode(
                        List.of(
                                new IfStatementNode(
                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 4, 3, 1)),
                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 10, 9, 1)),
                                        new MemberAccessExpressionNode(
                                                new NameExpressionNode("game", new SingleLineTextRange(1, 5, 4, 4)),
                                                new NameExpressionNode("", new SingleLineTextRange(1, 10, 9, 0)),
                                                new SingleLineTextRange(1, 5, 4, 5)),
                                        new InvalidStatementNode(new SingleLineTextRange(1, 12, 11, 0)),
                                        null,
                                        new SingleLineTextRange(1, 1, 0, 11))),
                        new SingleLineTextRange(1, 1, 0, 11)));
    }

    @Test
    public void unfinishedBinaryExpressionTest() {
        ParserOutput result = parse("""
                return a > b || ;
                """);

        Assertions.assertIterableEquals(
                result.diagnostics(),
                List.of(
                        new DiagnosticMessage(ParserErrors.ExpressionExpected, new SingleLineTextRange(1, 17, 16, 1), ";")));

        Assertions.assertEquals(
                result.unit().statements,
                new StatementsListNode(
                        List.of(
                                new ReturnStatementNode(
                                        new BinaryExpressionNode(
                                                new BinaryExpressionNode(
                                                        new NameExpressionNode("a", new SingleLineTextRange(1, 8, 7, 1)),
                                                        new BinaryOperatorNode(BinaryOperator.GREATER, new SingleLineTextRange(1, 10, 9, 1)),
                                                        new NameExpressionNode("b", new SingleLineTextRange(1, 12, 11, 1)),
                                                        new SingleLineTextRange(1, 8, 7, 5)),
                                                new BinaryOperatorNode(BinaryOperator.BOOLEAN_OR, new SingleLineTextRange(1, 14, 13, 2)),
                                                new InvalidExpressionNode(new SingleLineTextRange(1, 17, 16, 0)),
                                                new SingleLineTextRange(1, 8, 7, 8)),
                                        new SingleLineTextRange(1, 1, 0, 17))),
                        new SingleLineTextRange(1, 1, 0, 17)));
    }

    @Test
    public void unfinishedArrayDeclarationTest() {
        ParserOutput result = parse("""
                int[] x = new int[] { 1 ;
                """);

        Assertions.assertIterableEquals(
                result.diagnostics(),
                List.of(
                        new DiagnosticMessage(ParserErrors.CommaOrCloseCurlyBracketExpected, new SingleLineTextRange(1, 25, 24, 1))));

        Assertions.assertEquals(
                result.unit().statements,
                new StatementsListNode(
                        List.of(
                                new VariableDeclarationNode(
                                        new ArrayTypeNode(
                                                new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 1, 0, 3)),
                                                new SingleLineTextRange(1, 1, 0, 5)),
                                        new NameExpressionNode("x", new SingleLineTextRange(1, 7, 6, 1)),
                                        new ArrayInitializerExpressionNode(
                                                new ArrayTypeNode(
                                                        new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 15, 14, 3)),
                                                        new SingleLineTextRange(1, 15, 14, 5)),
                                                List.of(
                                                        new IntegerLiteralExpressionNode("1", new SingleLineTextRange(1, 23, 22, 1))),
                                                new SingleLineTextRange(1, 11, 10, 13)),
                                        new SingleLineTextRange(1, 1, 0, 25))),
                        new SingleLineTextRange(1, 1, 0, 25)));
    }

    @Test
    public void unfinishedStatement1Test() {
        ParserOutput result = parse("""
                f
                freeCam.toggle();
                """);

        Assertions.assertIterableEquals(
                result.diagnostics(),
                List.of(
                        new DiagnosticMessage(ParserErrors.SemicolonExpected, new SingleLineTextRange(1, 1, 0, 1))));

        Assertions.assertIterableEquals(
                result.unit().statements.statements,
                List.of(
                        new ExpressionStatementNode(
                                new NameExpressionNode("f", new SingleLineTextRange(1, 1, 0, 1)),
                                new SingleLineTextRange(1, 1, 0, 1)),
                        new ExpressionStatementNode(
                                new InvocationExpressionNode(
                                        new MemberAccessExpressionNode(
                                                new NameExpressionNode("freeCam", new SingleLineTextRange(2, 1, 2, 7)),
                                                new NameExpressionNode("toggle", new SingleLineTextRange(2, 9, 10, 6)),
                                                new SingleLineTextRange(2, 1, 2, 14)),
                                        new ArgumentsListNode(List.of(), new SingleLineTextRange(2, 15, 16, 2)),
                                        new SingleLineTextRange(2, 1, 2, 16)),
                                new SingleLineTextRange(2, 1, 2, 17))));
    }

    @Test
    public void unfinishedStatement2Test() {
        ParserOutput result = parse("""
                int
                freeCam.toggle();
                """);

        Assertions.assertIterableEquals(
                result.diagnostics(),
                List.of(
                        new DiagnosticMessage(ParserErrors.IdentifierExpected, new SingleLineTextRange(2, 1, 4, 7), "freeCam."),
                        new DiagnosticMessage(ParserErrors.SemicolonExpected, new SingleLineTextRange(1, 1, 0, 3))));

        Assertions.assertIterableEquals(
                result.unit().statements.statements,
                List.of(
                        new VariableDeclarationNode(
                                new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 1, 0, 3)),
                                new NameExpressionNode("", new SingleLineTextRange(2, 1, 4, 0)),
                                new SingleLineTextRange(1, 1, 0, 3)),
                        new ExpressionStatementNode(
                                new InvocationExpressionNode(
                                        new MemberAccessExpressionNode(
                                                new NameExpressionNode("freeCam", new SingleLineTextRange(2, 1, 4, 7)),
                                                new NameExpressionNode("toggle", new SingleLineTextRange(2, 9, 12, 6)),
                                                new SingleLineTextRange(2, 1, 4, 14)),
                                        new ArgumentsListNode(List.of(), new SingleLineTextRange(2, 15, 18, 2)),
                                        new SingleLineTextRange(2, 1, 4, 16)),
                                new SingleLineTextRange(2, 1, 4, 17))));
    }

    @Test
    public void unfinishedStatement3Test() {
        ParserOutput result = parse("""
                if
                freeCam.toggle();
                """);

        Assertions.assertIterableEquals(
                result.diagnostics(),
                List.of(
                        new DiagnosticMessage(ParserErrors.LeftParenthesisExpected, new SingleLineTextRange(2, 1, 3, 7), "freeCam")));

        Assertions.assertIterableEquals(
                result.unit().statements.statements,
                List.of(
                        new IfStatementNode(
                                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(2, 1, 3, 0)),
                                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(2, 1, 3, 0)),
                                new InvalidExpressionNode(new SingleLineTextRange(2, 1, 3, 0)),
                                new InvalidStatementNode(new SingleLineTextRange(2, 1, 3, 0)),
                                null,
                                new SingleLineTextRange(1, 1, 0, 2)),
                        new ExpressionStatementNode(
                                new InvocationExpressionNode(
                                        new MemberAccessExpressionNode(
                                                new NameExpressionNode("freeCam", new SingleLineTextRange(2, 1, 3, 7)),
                                                new NameExpressionNode("toggle", new SingleLineTextRange(2, 9, 11, 6)),
                                                new SingleLineTextRange(2, 1, 3, 14)),
                                        new ArgumentsListNode(List.of(), new SingleLineTextRange(2, 15, 17, 2)),
                                        new SingleLineTextRange(2, 1, 3, 16)),
                                new SingleLineTextRange(2, 1, 3, 17))));
    }

    @Test
    public void unfinishedStatement4Test() {
        ParserOutput result = parse("""
                for
                freeCam.toggle();
                """);

        Assertions.assertIterableEquals(
                result.diagnostics(),
                List.of(
                        new DiagnosticMessage(ParserErrors.LeftParenthesisExpected, new SingleLineTextRange(2, 1, 4, 7), "freeCam")));

        Assertions.assertEquals(
                result.unit(),
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ForLoopStatementNode(
                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 4, 3, 0)),
                                        new InvalidStatementNode(new SingleLineTextRange(1, 4, 3, 0)),
                                        new InvalidExpressionNode(new SingleLineTextRange(1, 4, 3, 0)),
                                        new InvalidStatementNode(new SingleLineTextRange(1, 4, 3, 0)),
                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 4, 3, 0)),
                                        new InvalidStatementNode(new SingleLineTextRange(1, 4, 3, 0)),
                                        new SingleLineTextRange(1, 1, 0, 3)),
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new MemberAccessExpressionNode(
                                                        new NameExpressionNode("freeCam", new SingleLineTextRange(2, 1, 4, 7)),
                                                        new NameExpressionNode("toggle", new SingleLineTextRange(2, 9, 12, 6)),
                                                        new SingleLineTextRange(2, 1, 4, 14)),
                                                new ArgumentsListNode(List.of(), new SingleLineTextRange(2, 15, 18, 2)),
                                                new SingleLineTextRange(2, 1, 4, 16)),
                                        new SingleLineTextRange(2, 1, 4, 17))),
                                new MultiLineTextRange(1, 1, 2, 18, 0, 21)),
                        new MultiLineTextRange(1, 1, 2, 18, 0, 21)));
    }

    @Test
    public void unfinishedStatement5Test() {
        ParserOutput result = parse("""
                foreach
                freeCam.toggle();
                """);

        Assertions.assertIterableEquals(
                result.diagnostics(),
                List.of(
                        new DiagnosticMessage(ParserErrors.LeftParenthesisExpected, new SingleLineTextRange(2, 1, 8, 7), "freeCam")));

        Assertions.assertEquals(
                result.unit(),
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(),new SingleLineTextRange(1, 1, 0, 0)),
                        new StatementsListNode(List.of(
                                new ForEachLoopStatementNode(
                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 8, 7, 0)),
                                        new InvalidTypeNode(new SingleLineTextRange(1, 8, 7, 0)),
                                        new NameExpressionNode("", new SingleLineTextRange(1, 8, 7, 0)),
                                        new InvalidExpressionNode(new SingleLineTextRange(1, 8, 7, 0)),
                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 8, 7, 0)),
                                        new InvalidStatementNode(new SingleLineTextRange(1, 8, 7, 0)),
                                        new SingleLineTextRange(1, 1, 0, 7)),
                                new ExpressionStatementNode(
                                        new InvocationExpressionNode(
                                                new MemberAccessExpressionNode(
                                                        new NameExpressionNode("freeCam", new SingleLineTextRange(2, 1, 8, 7)),
                                                        new NameExpressionNode("toggle", new SingleLineTextRange(2, 9, 16, 6)),
                                                        new SingleLineTextRange(2, 1, 8, 14)),
                                                new ArgumentsListNode(List.of(), new SingleLineTextRange(2, 15, 22, 2)),
                                                new SingleLineTextRange(2, 1, 8, 16)),
                                        new SingleLineTextRange(2, 1, 8, 17))),
                                new MultiLineTextRange(1, 1, 2, 18, 0, 25)),
                        new MultiLineTextRange(1, 1, 2, 18, 0, 25)));
    }

    @Test
    public void unfinishedStatement6Test() {
        ParserOutput result = parse("""
                while
                freeCam.toggle();
                """);

        Assertions.assertIterableEquals(
                result.diagnostics(),
                List.of(
                        new DiagnosticMessage(ParserErrors.LeftParenthesisExpected, new SingleLineTextRange(2, 1, 6, 7), "freeCam")));

        Assertions.assertIterableEquals(
                result.unit().statements.statements,
                List.of(
                        new WhileLoopStatementNode(
                                new InvalidExpressionNode(new SingleLineTextRange(2, 1, 6, 0)),
                                new InvalidStatementNode(new SingleLineTextRange(2, 1, 6, 0)),
                                new SingleLineTextRange(1, 1, 0, 5)),
                        new ExpressionStatementNode(
                                new InvocationExpressionNode(
                                        new MemberAccessExpressionNode(
                                                new NameExpressionNode("freeCam", new SingleLineTextRange(2, 1, 6, 7)),
                                                new NameExpressionNode("toggle", new SingleLineTextRange(2, 9, 14, 6)),
                                                new SingleLineTextRange(2, 1, 6, 14)),
                                        new ArgumentsListNode(List.of(), new SingleLineTextRange(2, 15, 20, 2)),
                                        new SingleLineTextRange(2, 1, 6, 16)),
                                new SingleLineTextRange(2, 1, 6, 17))));
    }

    @Test
    public void unfinishedFunctionTest() {
        ParserOutput result = parse("""
                void abc
                freeCam.toggle();
                """);

        Assertions.assertIterableEquals(
                result.diagnostics(),
                List.of(
                        new DiagnosticMessage(ParserErrors.LeftParenthesisExpected, new SingleLineTextRange(2, 1, 9, 7), "freeCam")));

        Assertions.assertEquals(
                result.unit(),
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(
                                List.of(
                                        new FunctionNode(
                                                new ModifiersNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                                                new VoidTypeNode(new SingleLineTextRange(1, 1, 0, 4)),
                                                new NameExpressionNode("abc", new SingleLineTextRange(1, 6, 5, 3)),
                                                new ParameterListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 9, 8, 0)),
                                                        List.of(),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 9, 8, 0)),
                                                        new SingleLineTextRange(1, 9, 8, 0)),
                                                new BlockStatementNode(List.of(), new SingleLineTextRange(1, 9, 8, 0)),
                                                new SingleLineTextRange(1, 1, 0, 8))),
                                new SingleLineTextRange(1, 1, 0, 8)),
                        new StatementsListNode(
                                List.of(
                                        new ExpressionStatementNode(
                                                new InvocationExpressionNode(
                                                        new MemberAccessExpressionNode(
                                                                new NameExpressionNode("freeCam", new SingleLineTextRange(2, 1, 9, 7)),
                                                                new NameExpressionNode("toggle", new SingleLineTextRange(2, 9, 17, 6)),
                                                                new SingleLineTextRange(2, 1, 9, 14)),
                                                        new ArgumentsListNode(List.of(), new SingleLineTextRange(2, 15, 23, 2)),
                                                        new SingleLineTextRange(2, 1, 9, 16)),
                                                new SingleLineTextRange(2, 1, 9, 17))),
                                new SingleLineTextRange(2, 1, 9, 17)),
                        new MultiLineTextRange(1, 1, 2, 18, 0, 26)));
    }

    private ParserOutput parse(String code) {
        return new Parser(new Lexer(new LexerInput(code)).lex()).parse();
    }
}