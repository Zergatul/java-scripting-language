package com.zergatul.scripting.tests;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.*;
import com.zergatul.scripting.parser.nodes.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ParserTests {

    @Test
    public void emptyCodeTest() {
        ParserOutput result = parse("");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new SingleLineTextRange(1, 1, 0, 0)));
    }

    @Test
    public void emptyBlockStatementTest() {
        ParserOutput result = parse("{}");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(
                        List.of(new BlockStatementNode(
                                new ArrayList<>(),
                                new SingleLineTextRange(1, 1, 0, 2))),
                        new SingleLineTextRange(1, 1, 0, 2)),
                new SingleLineTextRange(1, 1, 0, 2)));
    }

    @Test
    public void simpleVariableDeclarationTest() {
        ParserOutput result = parse("int x;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new VariableDeclarationNode(
                                new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 1, 0, 3)),
                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
                                null,
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 6, 5, 1)),
                                new SingleLineTextRange(1, 1, 0, 6))),
                        new SingleLineTextRange(1, 1, 0, 6)),
                new SingleLineTextRange(1, 1, 0, 6)));
    }

    @Test
    public void simpleVariableDeclarationWithInitializerTest() {
        ParserOutput result = parse("int x = 10;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new VariableDeclarationNode(
                                new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 1, 0, 3)),
                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
                                new IntegerLiteralExpressionNode("10", new SingleLineTextRange(1, 9, 8, 2)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 11, 10, 1)),
                                new SingleLineTextRange(1, 1, 0, 11))),
                        new SingleLineTextRange(1, 1, 0, 11)),
                new SingleLineTextRange(1, 1, 0, 11)));
    }

    @Test
    public void arrayVariableDeclarationTest() {
        ParserOutput result = parse("int[][] x;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
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
                new SingleLineTextRange(1, 1, 0, 10)));
    }

    @Test
    public void simpleAssignmentTest() {
        ParserOutput result = parse("a = b + c;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
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
                new SingleLineTextRange(1, 1, 0, 10)));
    }

    @Test
    public void assignmentOperatorsTest() {
        ParserOutput result = parse("a += 10; b -= 15; c *= 2; d /= 2;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                new AssignmentStatementNode(
                        new NameExpressionNode("a", new SingleLineTextRange(1, 1, 0, 1)),
                        new AssignmentOperatorNode(AssignmentOperator.PLUS_ASSIGNMENT, new SingleLineTextRange(1, 3, 2, 2)),
                        new IntegerLiteralExpressionNode("10", new SingleLineTextRange(1, 6, 5, 2)),
                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 8, 7, 1)),
                        new SingleLineTextRange(1, 1, 0, 8)),
                new AssignmentStatementNode(
                        new NameExpressionNode("b", new SingleLineTextRange(1, 10, 9, 1)),
                        new AssignmentOperatorNode(AssignmentOperator.MINUS_ASSIGNMENT, new SingleLineTextRange(1, 12, 11, 2)),
                        new IntegerLiteralExpressionNode("15", new SingleLineTextRange(1, 15, 14, 2)),
                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 17, 16, 1)),
                        new SingleLineTextRange(1, 10, 9, 8)),
                new AssignmentStatementNode(
                        new NameExpressionNode("c", new SingleLineTextRange(1, 19, 18, 1)),
                        new AssignmentOperatorNode(AssignmentOperator.MULTIPLY_ASSIGNMENT, new SingleLineTextRange(1, 21, 20, 2)),
                        new IntegerLiteralExpressionNode("2", new SingleLineTextRange(1, 24, 23, 1)),
                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 25, 24, 1)),
                        new SingleLineTextRange(1, 19, 18, 7)),
                new AssignmentStatementNode(
                        new NameExpressionNode("d", new SingleLineTextRange(1, 27, 26, 1)),
                        new AssignmentOperatorNode(AssignmentOperator.DIVIDE_ASSIGNMENT, new SingleLineTextRange(1, 29, 28, 2)),
                        new IntegerLiteralExpressionNode("2", new SingleLineTextRange(1, 32, 31, 1)),
                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 33, 32, 1)),
                        new SingleLineTextRange(1, 27, 26, 7))),
                new SingleLineTextRange(1, 1, 0, 33)),
                new SingleLineTextRange(1, 1, 0, 33)));
    }

    @Test
    public void invokeExpressionTest() {
        ParserOutput result = parse("abc.qwe.x();");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(new ExpressionStatementNode(
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
                        new SingleLineTextRange(1, 1, 0, 12)
                        )),
                        new SingleLineTextRange(1, 1, 0, 12)),
                new SingleLineTextRange(1, 1, 0, 12)));
    }

    @Test
    public void unaryOperatorTest() {
        ParserOutput result = parse("a = -b;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new AssignmentStatementNode(
                                new NameExpressionNode("a", new SingleLineTextRange(1, 1, 0, 1)),
                                new AssignmentOperatorNode(AssignmentOperator.ASSIGNMENT, new SingleLineTextRange(1, 3, 2, 1)),
                                new UnaryExpressionNode(
                                        new UnaryOperatorNode(UnaryOperator.MINUS, new SingleLineTextRange(1, 5, 4, 1)),
                                        new NameExpressionNode("b", new SingleLineTextRange(1, 6, 5, 1)),
                                        new SingleLineTextRange(1, 5, 4, 2)
                                ),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 7, 6, 1)),
                                new SingleLineTextRange(1, 1, 0, 7))),
                        new SingleLineTextRange(1, 1, 0, 7)),
                new SingleLineTextRange(1, 1, 0, 7)));
    }

    @Test
    public void negativeIntegerTest() {
        ParserOutput result = parse("x = -100;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new AssignmentStatementNode(
                                new NameExpressionNode("x", new SingleLineTextRange(1, 1, 0, 1)),
                                new AssignmentOperatorNode(AssignmentOperator.ASSIGNMENT, new SingleLineTextRange(1, 3, 2, 1)),
                                new IntegerLiteralExpressionNode("-100", new SingleLineTextRange(1, 5, 4, 4)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 9, 8, 1)),
                                new SingleLineTextRange(1, 1, 0, 9))),
                        new SingleLineTextRange(1, 1, 0, 9)),
                new SingleLineTextRange(1, 1, 0, 9)));
    }

    @Test
    public void unaryMinusTest() {
        ParserOutput result = parse("x = -1 + 1;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
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
                new SingleLineTextRange(1, 1, 0, 11)));
    }

    @Test
    public void ifStatementTest1() {
        ParserOutput result = parse("if (a) b = 3;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new IfStatementNode(
                                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 4, 3, 1)),
                                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 6, 5, 1)),
                                new NameExpressionNode("a", new SingleLineTextRange(1, 5, 4, 1)),
                                new AssignmentStatementNode(
                                        new NameExpressionNode("b", new SingleLineTextRange(1, 8, 7, 1)),
                                        new AssignmentOperatorNode(AssignmentOperator.ASSIGNMENT, new SingleLineTextRange(1, 10, 9, 1)),
                                        new IntegerLiteralExpressionNode("3", new SingleLineTextRange(1, 12, 11, 1)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 13, 12, 1)),
                                        new SingleLineTextRange(1, 8, 7, 6)),
                                null,
                                new SingleLineTextRange(1, 1, 0, 13))),
                        new SingleLineTextRange(1, 1, 0, 13)),
                new SingleLineTextRange(1, 1, 0, 13)));
    }

    @Test
    public void ifStatementTest2() {
        ParserOutput result = parse("if (a) b = 3;else{}");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new IfStatementNode(
                                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 4, 3, 1)),
                                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 6, 5, 1)),
                                new NameExpressionNode("a", new SingleLineTextRange(1, 5, 4, 1)),
                                new AssignmentStatementNode(
                                        new NameExpressionNode("b", new SingleLineTextRange(1, 8, 7, 1)),
                                        new AssignmentOperatorNode(AssignmentOperator.ASSIGNMENT, new SingleLineTextRange(1, 10, 9, 1)),
                                        new IntegerLiteralExpressionNode("3", new SingleLineTextRange(1, 12, 11, 1)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 13, 12, 1)),
                                        new SingleLineTextRange(1, 8, 7, 6)),
                                new BlockStatementNode(List.of(), new SingleLineTextRange(1, 18, 17, 2)),
                                new SingleLineTextRange(1, 1, 0, 19))),
                        new SingleLineTextRange(1, 1, 0, 19)),
                new SingleLineTextRange(1, 1, 0, 19)));
    }

    @Test
    public void returnStatementTest1() {
        ParserOutput result = parse("return;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new ReturnStatementNode(
                                new Token(TokenType.RETURN, new SingleLineTextRange(1, 1, 0, 6)),
                                null,
                                new SingleLineTextRange(1, 1, 0, 7))),
                        new SingleLineTextRange(1, 1, 0, 7)),
                new SingleLineTextRange(1, 1, 0, 7)));
    }

    @Test
    public void returnStatementTest2() {
        ParserOutput result = parse("return true;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new ReturnStatementNode(
                                new Token(TokenType.RETURN, new SingleLineTextRange(1, 1, 0, 6)),
                                new BooleanLiteralExpressionNode(true, new SingleLineTextRange(1, 8, 7, 4)),
                                new SingleLineTextRange(1, 1, 0, 12))),
                        new SingleLineTextRange(1, 1, 0, 12)),
                new SingleLineTextRange(1, 1, 0, 12)));
    }

    @Test
    public void lambdaTest1() {
        ParserOutput result = parse("func(() => 1);");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new ExpressionStatementNode(
                                new InvocationExpressionNode(
                                        new NameExpressionNode("func", new SingleLineTextRange(1, 1, 0, 4)),
                                        new ArgumentsListNode(List.of(
                                                new LambdaExpressionNode(
                                                        List.of(),
                                                        new Token(TokenType.EQUAL_GREATER, new SingleLineTextRange(1, 9, 8, 2)),
                                                        new ExpressionStatementNode(
                                                                new IntegerLiteralExpressionNode("1", new SingleLineTextRange(1, 12, 11, 1)),
                                                                new SingleLineTextRange(1, 12, 11, 1)),
                                                        new SingleLineTextRange(1, 6, 5, 7))),
                                                new SingleLineTextRange(1, 5, 4, 9)),
                                        new SingleLineTextRange(1, 1, 0, 13)),
                                new SingleLineTextRange(1, 1, 0, 14))),
                        new SingleLineTextRange(1, 1, 0, 14)),
                new SingleLineTextRange(1, 1, 0, 14)));
    }

    @Test
    public void lambdaTest2() {
        ParserOutput result = parse("func(a => {});");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new ExpressionStatementNode(
                                new InvocationExpressionNode(
                                        new NameExpressionNode("func", new SingleLineTextRange(1, 1, 0, 4)),
                                        new ArgumentsListNode(List.of(
                                                new LambdaExpressionNode(
                                                        List.of(
                                                                new NameExpressionNode(
                                                                    new IdentifierToken("a", new SingleLineTextRange(1, 6, 5, 1)))),
                                                        new Token(TokenType.EQUAL_GREATER, new SingleLineTextRange(1, 8, 7, 2)),
                                                        new BlockStatementNode(
                                                                List.of(),
                                                                new SingleLineTextRange(1, 11, 10, 2)),
                                                        new SingleLineTextRange(1, 6, 5, 7))),
                                                new SingleLineTextRange(1, 5, 4, 9)),
                                        new SingleLineTextRange(1, 1, 0, 13)),
                                new SingleLineTextRange(1, 1, 0, 14))),
                        new SingleLineTextRange(1, 1, 0, 14)),
                new SingleLineTextRange(1, 1, 0, 14)));
    }

    @Test
    public void lambdaTest3() {
        ParserOutput result = parse("func((a, b, c) => {});");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new ExpressionStatementNode(
                                new InvocationExpressionNode(
                                        new NameExpressionNode("func", new SingleLineTextRange(1, 1, 0, 4)),
                                        new ArgumentsListNode(List.of(
                                                new LambdaExpressionNode(
                                                        List.of(
                                                                new NameExpressionNode(
                                                                        new IdentifierToken("a", new SingleLineTextRange(1, 7, 6, 1))),
                                                                new NameExpressionNode(
                                                                        new IdentifierToken("b", new SingleLineTextRange(1, 10, 9, 1))),
                                                                new NameExpressionNode(
                                                                        new IdentifierToken("c", new SingleLineTextRange(1, 13, 12, 1)))),
                                                        new Token(TokenType.EQUAL_GREATER, new SingleLineTextRange(1, 16, 15, 2)),
                                                        new BlockStatementNode(
                                                                List.of(),
                                                                new SingleLineTextRange(1, 19, 18, 2)),
                                                        new SingleLineTextRange(1, 6, 5, 15))),
                                                new SingleLineTextRange(1, 5, 4, 17)),
                                        new SingleLineTextRange(1, 1, 0, 21)),
                                new SingleLineTextRange(1, 1, 0, 22))),
                        new SingleLineTextRange(1, 1, 0, 22)),
                new SingleLineTextRange(1, 1, 0, 22)));
    }

    @Test
    public void functionTest1() {
        ParserOutput result = parse("void a(){}");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(
                        new FunctionNode(
                                new ModifiersNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                                new VoidTypeNode(new SingleLineTextRange(1, 1, 0, 4)),
                                new NameExpressionNode(
                                        new IdentifierToken("a", new SingleLineTextRange(1, 6, 5, 1))),
                                new ParameterListNode(
                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 7, 6, 1)),
                                        List.of(),
                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 8, 7, 1)),
                                        new SingleLineTextRange(1, 7, 6, 2)),
                                new BlockStatementNode(List.of(), new SingleLineTextRange(1, 9, 8, 2)),
                                new SingleLineTextRange(1, 1, 0, 10))
                ), new SingleLineTextRange(1, 1, 0, 10)),
                new StatementsListNode(List.of(), new SingleLineTextRange(1, 11, 10, 0)),
                new SingleLineTextRange(1, 1, 0, 10)));
    }

    @Test
    public void functionTest2() {
        ParserOutput result = parse("int[][][] a(int[][][] b, string s) {}");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(
                        new FunctionNode(
                                new ModifiersNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                                new ArrayTypeNode(
                                        new ArrayTypeNode(
                                                new ArrayTypeNode(
                                                        new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 1, 0, 3)),
                                                        new SingleLineTextRange(1, 1, 0, 5)),
                                                new SingleLineTextRange(1, 1, 0, 7)),
                                        new SingleLineTextRange(1, 1, 0, 9)),
                                new NameExpressionNode(
                                        new IdentifierToken("a", new SingleLineTextRange(1, 11, 10, 1))),
                                new ParameterListNode(
                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 12, 11, 1)),
                                        List.of(
                                                new ParameterNode(
                                                        new ArrayTypeNode(
                                                                new ArrayTypeNode(
                                                                        new ArrayTypeNode(
                                                                                new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 13, 12, 3)),
                                                                                new SingleLineTextRange(1, 13, 12, 5)),
                                                                        new SingleLineTextRange(1, 13, 12, 7)),
                                                                new SingleLineTextRange(1, 13, 12, 9)),
                                                        new NameExpressionNode(
                                                            new IdentifierToken("b", new SingleLineTextRange(1, 23, 22, 1))),
                                                        new SingleLineTextRange(1, 13, 12, 11)),
                                                new ParameterNode(
                                                        new PredefinedTypeNode(PredefinedType.STRING, new SingleLineTextRange(1, 26, 25, 6)),
                                                        new NameExpressionNode(
                                                            new IdentifierToken("s", new SingleLineTextRange(1, 33, 32, 1))),
                                                        new SingleLineTextRange(1, 26, 25, 8))),
                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 34, 33, 1)),
                                        new SingleLineTextRange(1, 12, 11, 23)),
                                new BlockStatementNode(List.of(), new SingleLineTextRange(1, 36, 35, 2)),
                                new SingleLineTextRange(1, 1, 0, 37))),
                        new SingleLineTextRange(1, 1, 0, 37)),
                new StatementsListNode(List.of(), new SingleLineTextRange(1, 38, 37, 0)),
                new SingleLineTextRange(1, 1, 0, 37)));
    }

    @Test
    public void endOfFileDiagnosticsTest() {
        ParserOutput result = parse("a()");
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(ParserErrors.SemicolonExpected, new SingleLineTextRange(1, 3, 2, 1))));
    }

    @Test
    public void functionAfterVariableTest() {
        ParserOutput result = parse("""
                int x = 0;
                int func(){ return 1; }
                """);
        Assertions.assertFalse(result.diagnostics().isEmpty());
        Assertions.assertEquals(
                result.diagnostics().get(0),
                new DiagnosticMessage(
                        ParserErrors.SemicolonOrEqualExpected,
                        new SingleLineTextRange(2, 9, 19, 1),
                        "("));
    }

    @Test
    public void leftAssociativityTest() {
        ParserOutput result = parse("""
                int x = 1 + 2 + 3;
                """);
        Assertions.assertTrue(result.diagnostics().isEmpty());
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new VariableDeclarationNode(
                                new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 1, 0, 3)),
                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
                                new BinaryExpressionNode(
                                        new BinaryExpressionNode(
                                                new IntegerLiteralExpressionNode("1", new SingleLineTextRange(1, 9, 8, 1)),
                                                new BinaryOperatorNode(BinaryOperator.PLUS, new SingleLineTextRange(1, 11, 10, 1)),
                                                new IntegerLiteralExpressionNode("2", new SingleLineTextRange(1, 13, 12, 1)),
                                                new SingleLineTextRange(1, 9, 8, 5)),
                                        new BinaryOperatorNode(BinaryOperator.PLUS, new SingleLineTextRange(1, 15, 14, 1)),
                                        new IntegerLiteralExpressionNode("3", new SingleLineTextRange(1, 17, 16, 1)),
                                        new SingleLineTextRange(1, 9, 8, 9)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 18, 17, 1)),
                                new SingleLineTextRange(1, 1, 0, 18))),
                        new SingleLineTextRange(1, 1, 0, 18)),
                new SingleLineTextRange(1, 1, 0, 18)));
    }

    @Test
    public void letTest() {
        ParserOutput result = parse("""
                let x = 123;
                """);
        Assertions.assertTrue(result.diagnostics().isEmpty());
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new VariableDeclarationNode(
                                new LetTypeNode(new SingleLineTextRange(1, 1, 0, 3)),
                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
                                new IntegerLiteralExpressionNode("123", new SingleLineTextRange(1, 9, 8, 3)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 12, 11, 1)),
                                new SingleLineTextRange(1, 1, 0, 12))),
                        new SingleLineTextRange(1, 1, 0, 12)),
                new SingleLineTextRange(1, 1, 0, 12)));
    }

    @Test
    public void typeTestExpressionTest1() {
        ParserOutput result = parse("""
                let x = a is string || b;
                """);
        Assertions.assertTrue(result.diagnostics().isEmpty());
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new VariableDeclarationNode(
                                new LetTypeNode(new SingleLineTextRange(1, 1, 0, 3)),
                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
                                new BinaryExpressionNode(
                                        new TypeTestExpressionNode(
                                                new NameExpressionNode("a", new SingleLineTextRange(1, 9, 8, 1)),
                                                new PredefinedTypeNode(PredefinedType.STRING, new SingleLineTextRange(1, 14, 13, 6)),
                                                new SingleLineTextRange(1, 9, 8, 11)),
                                        new BinaryOperatorNode(BinaryOperator.BOOLEAN_OR, new SingleLineTextRange(1, 21, 20, 2)),
                                        new NameExpressionNode("b", new SingleLineTextRange(1, 24, 23, 1)),
                                        new SingleLineTextRange(1, 9, 8, 16)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 25, 24, 1)),
                                new SingleLineTextRange(1, 1, 0, 25))),
                        new SingleLineTextRange(1, 1, 0, 25)),
                new SingleLineTextRange(1, 1, 0, 25)));
    }

    @Test
    public void typeTestExpressionTest2() {
        ParserOutput result = parse("""
                let x = a == b is string;
                """);
        Assertions.assertTrue(result.diagnostics().isEmpty());
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new VariableDeclarationNode(
                                new LetTypeNode(new SingleLineTextRange(1, 1, 0, 3)),
                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
                                new BinaryExpressionNode(
                                        new NameExpressionNode("a", new SingleLineTextRange(1, 9, 8, 1)),
                                        new BinaryOperatorNode(BinaryOperator.EQUALS, new SingleLineTextRange(1, 11, 10, 2)),
                                        new TypeTestExpressionNode(
                                                new NameExpressionNode("b", new SingleLineTextRange(1, 14, 13, 1)),
                                                new PredefinedTypeNode(PredefinedType.STRING, new SingleLineTextRange(1, 19, 18, 6)),
                                                new SingleLineTextRange(1, 14, 13, 11)),
                                        new SingleLineTextRange(1, 9, 8, 16)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 25, 24, 1)),
                                new SingleLineTextRange(1, 1, 0, 25))),
                        new SingleLineTextRange(1, 1, 0, 25)),
                new SingleLineTextRange(1, 1, 0, 25)));
    }

    @Test
    public void metaTest() {
        ParserOutput result = parse("""
                let x = #typeof(1) == #type(int);
                """);
        Assertions.assertTrue(result.diagnostics().isEmpty());
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new VariableDeclarationNode(
                                new LetTypeNode(new SingleLineTextRange(1, 1, 0, 3)),
                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
                                new BinaryExpressionNode(
                                        new MetaTypeOfExpressionNode(
                                                new Token(TokenType.META_TYPE_OF, new SingleLineTextRange(1, 9, 8, 7)),
                                                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 16, 15, 1)),
                                                new IntegerLiteralExpressionNode("1", new SingleLineTextRange(1, 17, 16, 1)),
                                                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 18, 17, 1)),
                                                new SingleLineTextRange(1, 9, 8, 10)),
                                        new BinaryOperatorNode(BinaryOperator.EQUALS, new SingleLineTextRange(1, 20, 19, 2)),
                                        new MetaTypeExpressionNode(
                                                new Token(TokenType.META_TYPE, new SingleLineTextRange(1, 23, 22, 5)),
                                                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 28, 27, 1)),
                                                new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 29, 28, 3)),
                                                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 32, 31, 1)),
                                                new SingleLineTextRange(1, 23, 22, 10)),
                                        new SingleLineTextRange(1, 9, 8, 24)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 33, 32, 1)),
                                new SingleLineTextRange(1, 1, 0, 33))),
                        new SingleLineTextRange(1, 1, 0, 33)),
                new SingleLineTextRange(1, 1, 0, 33)));
    }

    @Test
    public void javaRawTypeTest() {
        ParserOutput result = parse("""
                Java<com.example.ClassA> a;
                """);
        Assertions.assertTrue(result.diagnostics().isEmpty());
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new VariableDeclarationNode(
                                new JavaTypeNode(
                                        new Token(TokenType.LESS, new SingleLineTextRange(1, 5, 4, 1)),
                                        new JavaQualifiedTypeNameNode("com.example.ClassA", new SingleLineTextRange(1, 6, 5, 18)),
                                        new Token(TokenType.GREATER, new SingleLineTextRange(1, 24, 23, 1)),
                                        new SingleLineTextRange(1, 1, 0, 24)),
                                new NameExpressionNode("a", new SingleLineTextRange(1, 26, 25, 1)),
                                null,
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 27, 26, 1)),
                                new SingleLineTextRange(1, 1, 0, 27))),
                        new SingleLineTextRange(1, 1, 0, 27)),
                new SingleLineTextRange(1, 1, 0, 27)));
    }

    @Test
    public void newExpressionTest1() {
        ParserOutput result = parse("""
                let x = new int[10];
                """);
        Assertions.assertTrue(result.diagnostics().isEmpty());
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new VariableDeclarationNode(
                                new LetTypeNode(new SingleLineTextRange(1, 1, 0, 3)),
                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
                                new ArrayCreationExpressionNode(
                                        new ArrayTypeNode(
                                                new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 13, 12, 3)),
                                                new SingleLineTextRange(1, 13, 12, 7)),
                                        new IntegerLiteralExpressionNode("10", new SingleLineTextRange(1, 17, 16, 2)),
                                        new SingleLineTextRange(1, 9, 8, 11)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 20, 19, 1)),
                                new SingleLineTextRange(1, 1, 0, 20))),
                        new SingleLineTextRange(1, 1, 0, 20)),
                new SingleLineTextRange(1, 1, 0, 20)));
    }

    @Test
    public void newExpressionTest2() {
        ParserOutput result = parse("""
                let x = new int[] { 1, 2, 3 };
                """);
        Assertions.assertTrue(result.diagnostics().isEmpty());
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new VariableDeclarationNode(
                                new LetTypeNode(new SingleLineTextRange(1, 1, 0, 3)),
                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
                                new ArrayInitializerExpressionNode(
                                        new ArrayTypeNode(
                                                new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 13, 12, 3)),
                                                new SingleLineTextRange(1, 13, 12, 5)),
                                        List.of(
                                                new IntegerLiteralExpressionNode("1", new SingleLineTextRange(1, 21, 20, 1)),
                                                new IntegerLiteralExpressionNode("2", new SingleLineTextRange(1, 24, 23, 1)),
                                                new IntegerLiteralExpressionNode("3", new SingleLineTextRange(1, 27, 26, 1))),
                                        new SingleLineTextRange(1, 9, 8, 21)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 30, 29, 1)),
                                new SingleLineTextRange(1, 1, 0, 30))),
                        new SingleLineTextRange(1, 1, 0, 30)),
                new SingleLineTextRange(1, 1, 0, 30)));
    }

    @Test
    public void newExpressionTest3() {
        ParserOutput result = parse("""
                let x = new ClassA();
                """);
        Assertions.assertTrue(result.diagnostics().isEmpty());
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(List.of(
                        new VariableDeclarationNode(new LetTypeNode(new SingleLineTextRange(1, 1, 0, 3)),
                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
                                new ObjectCreationExpressionNode(
                                        new CustomTypeNode("ClassA", new SingleLineTextRange(1, 13, 12, 6)),
                                        new ArgumentsListNode(List.of(), new SingleLineTextRange(1, 19, 18, 2)),
                                        new SingleLineTextRange(1, 9, 8, 12)),
                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 21, 20, 1)),
                                new SingleLineTextRange(1, 1, 0, 21))),
                        new SingleLineTextRange(1, 1, 0, 21)),
                new SingleLineTextRange(1, 1, 0, 21)));
    }

    @Test
    public void customTypeTest() {
        ParserOutput result = parse("""
                CustomType a;
                """);
        Assertions.assertTrue(result.diagnostics().isEmpty());
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new CompilationUnitMembersListNode(
                        List.of(),
                        new SingleLineTextRange(1, 1, 0, 0)),
                new StatementsListNode(
                        List.of(
                                new VariableDeclarationNode(
                                        new CustomTypeNode("CustomType", new SingleLineTextRange(1, 1, 0, 10)),
                                        new NameExpressionNode("a", new SingleLineTextRange(1, 12, 11, 1)),
                                        null,
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 13, 12, 1)),
                                        new SingleLineTextRange(1, 1, 0, 13))),
                        new SingleLineTextRange(1, 1, 0, 13)),
                new SingleLineTextRange(1, 1, 0, 13)));
    }

    @Test
    public void notAStatementTest1() {
        ParserOutput result = parse("""
                a.b;
                """);
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(ParserErrors.NotAStatement, new SingleLineTextRange(1, 1, 0, 3))));
    }

    @Test
    public void notAStatementTest2() {
        ParserOutput result = parse("""
                a + b;
                """);
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(ParserErrors.NotAStatement, new SingleLineTextRange(1, 1, 0, 5))));
    }

    @Test
    public void notAStatementTest3() {
        ParserOutput result = parse("""
                "ab";
                """);
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(ParserErrors.NotAStatement, new SingleLineTextRange(1, 1, 0, 4))));
    }

    @Test
    public void notAStatementTest4() {
        ParserOutput result = parse("""
                (a == b);
                """);
        Assertions.assertIterableEquals(result.diagnostics(), List.of(
                new DiagnosticMessage(ParserErrors.NotAStatement, new SingleLineTextRange(1, 1, 0, 8))));
    }

    private ParserOutput parse(String code) {
        return new Parser(new Lexer(new LexerInput(code)).lex()).parse();
    }
}