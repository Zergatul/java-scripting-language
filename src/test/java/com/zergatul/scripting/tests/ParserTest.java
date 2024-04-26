package com.zergatul.scripting.tests;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.IdentifierToken;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.parser.*;
import com.zergatul.scripting.parser.nodes.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ParserTest {

    @Test
    public void emptyCodeTest() {
        var result = parse("");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                new ArrayList<>(),
                new SingleLineTextRange(1, 1, 0, 0)));
    }

    @Test
    public void emptyBlockStatementTest() {
        var result = parse("{}");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(new BlockStatementNode(
                        new ArrayList<>(),
                        new SingleLineTextRange(1, 1, 0, 2))),
                new SingleLineTextRange(1, 1, 0, 2)));
    }

    @Test
    public void simpleVariableDeclarationTest() {
        var result = parse("int x;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(new VariableDeclarationNode(
                        new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 1, 0, 3)),
                        new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
                        new SingleLineTextRange(1, 1, 0, 6)
                )),
                new SingleLineTextRange(1, 1, 0, 6)));
    }

    @Test
    public void simpleVariableDeclarationWithInitializerTest() {
        var result = parse("int x = 10;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(new VariableDeclarationNode(
                        new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 1, 0, 3)),
                        new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
                        new IntegerLiteralExpressionNode("10", new SingleLineTextRange(1, 9, 8, 2)),
                        new SingleLineTextRange(1, 1, 0, 11)
                )),
                new SingleLineTextRange(1, 1, 0, 11)));
    }

    @Test
    public void arrayVariableDeclarationTest() {
        var result = parse("int[][] x;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(new VariableDeclarationNode(
                        new ArrayTypeNode(
                                new ArrayTypeNode(
                                    new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 1, 0, 3)),
                                    new SingleLineTextRange(1, 1, 0, 5)),
                                new SingleLineTextRange(1, 1, 0, 7)),
                        new NameExpressionNode("x", new SingleLineTextRange(1, 9, 8, 1)),
                        new SingleLineTextRange(1, 1, 0, 10)
                )),
                new SingleLineTextRange(1, 1, 0, 10)));
    }

    @Test
    public void simpleAssignmentTest() {
        var result = parse("a = b + c;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(new AssignmentStatementNode(
                        new NameExpressionNode("a", new SingleLineTextRange(1, 1, 0, 1)),
                        new AssignmentOperatorNode(AssignmentOperator.ASSIGNMENT, new SingleLineTextRange(1, 3, 2, 1)),
                        new BinaryExpressionNode(
                                new NameExpressionNode("b", new SingleLineTextRange(1, 5, 4, 1)),
                                new BinaryOperatorNode(BinaryOperator.PLUS, new SingleLineTextRange(1, 7, 6, 1)),
                                new NameExpressionNode("c", new SingleLineTextRange(1, 9, 8, 1)),
                                new SingleLineTextRange(1, 5, 4, 5)),
                        new SingleLineTextRange(1, 1, 0, 10))),
                new SingleLineTextRange(1, 1, 0, 10)));
    }

    @Test
    public void assignmentOperatorsTest() {
        var result = parse("a += 10; b -= 15; c *= 2; d /= 2;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(List.of(
                new AssignmentStatementNode(
                        new NameExpressionNode("a", new SingleLineTextRange(1, 1, 0, 1)),
                        new AssignmentOperatorNode(AssignmentOperator.PLUS_ASSIGNMENT, new SingleLineTextRange(1, 3, 2, 2)),
                        new IntegerLiteralExpressionNode("10", new SingleLineTextRange(1, 6, 5, 2)),
                        new SingleLineTextRange(1, 1, 0, 8)),
                new AssignmentStatementNode(
                        new NameExpressionNode("b", new SingleLineTextRange(1, 10, 9, 1)),
                        new AssignmentOperatorNode(AssignmentOperator.MINUS_ASSIGNMENT, new SingleLineTextRange(1, 12, 11, 2)),
                        new IntegerLiteralExpressionNode("15", new SingleLineTextRange(1, 15, 14, 2)),
                        new SingleLineTextRange(1, 10, 9, 8)),
                new AssignmentStatementNode(
                        new NameExpressionNode("c", new SingleLineTextRange(1, 19, 18, 1)),
                        new AssignmentOperatorNode(AssignmentOperator.MULTIPLY_ASSIGNMENT, new SingleLineTextRange(1, 21, 20, 2)),
                        new IntegerLiteralExpressionNode("2", new SingleLineTextRange(1, 24, 23, 1)),
                        new SingleLineTextRange(1, 19, 18, 7)),
                new AssignmentStatementNode(
                        new NameExpressionNode("d", new SingleLineTextRange(1, 27, 26, 1)),
                        new AssignmentOperatorNode(AssignmentOperator.DIVIDE_ASSIGNMENT, new SingleLineTextRange(1, 29, 28, 2)),
                        new IntegerLiteralExpressionNode("2", new SingleLineTextRange(1, 32, 31, 1)),
                        new SingleLineTextRange(1, 27, 26, 7))),
                new SingleLineTextRange(1, 1, 0, 33)));
    }

    @Test
    public void invokeExpressionTest() {
        var result = parse("abc.qwe.x();");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(new ExpressionStatementNode(
                        new InvocationExpressionNode(
                            new MemberAccessExpressionNode(
                                    new MemberAccessExpressionNode(
                                            new NameExpressionNode("abc", new SingleLineTextRange(1, 1, 0, 3)),
                                            new NameExpressionNode("qwe", new SingleLineTextRange(1, 5, 4, 3)),
                                            new SingleLineTextRange(1, 1, 0, 7)),
                                    new NameExpressionNode("x", new SingleLineTextRange(1, 9, 8, 1)),
                                    new SingleLineTextRange(1, 1, 0, 9)),
                            new ArgumentsListNode(List.of(), new SingleLineTextRange(1, 10, 9, 2)),
                            new SingleLineTextRange(1, 1, 0, 11)),
                        new SingleLineTextRange(1, 1, 0, 12)
                        )),
                new SingleLineTextRange(1, 1, 0, 12)));
    }

    @Test
    public void unaryOperatorTest() {
        var result = parse("a = -b;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(
                        new AssignmentStatementNode(
                                new NameExpressionNode("a", new SingleLineTextRange(1, 1, 0, 1)),
                                new AssignmentOperatorNode(AssignmentOperator.ASSIGNMENT, new SingleLineTextRange(1, 3, 2, 1)),
                                new UnaryExpressionNode(
                                        new UnaryOperatorNode(UnaryOperator.MINUS, new SingleLineTextRange(1, 5, 4, 1)),
                                        new NameExpressionNode("b", new SingleLineTextRange(1, 6, 5, 1)),
                                        new SingleLineTextRange(1, 5, 4, 2)
                                ),
                                new SingleLineTextRange(1, 1, 0, 7))),
                new SingleLineTextRange(1, 1, 0, 7)));
    }

    @Test
    public void negativeIntegerTest() {
        var result = parse("x = -100;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(
                        new AssignmentStatementNode(
                            new NameExpressionNode("x", new SingleLineTextRange(1, 1, 0, 1)),
                            new AssignmentOperatorNode(AssignmentOperator.ASSIGNMENT, new SingleLineTextRange(1, 3, 2, 1)),
                            new IntegerLiteralExpressionNode("-100", new SingleLineTextRange(1, 5, 4, 4)),
                            new SingleLineTextRange(1, 1, 0, 9))),
                new SingleLineTextRange(1, 1, 0, 9)));
    }

    @Test
    public void unaryMinusTest() {
        var result = parse("x = -1 + 1;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(
                        new AssignmentStatementNode(
                                new NameExpressionNode("x", new SingleLineTextRange(1, 1, 0, 1)),
                                new AssignmentOperatorNode(AssignmentOperator.ASSIGNMENT, new SingleLineTextRange(1, 3, 2, 1)),
                                new BinaryExpressionNode(
                                        new IntegerLiteralExpressionNode("-1", new SingleLineTextRange(1, 5, 4, 2)),
                                        new BinaryOperatorNode(BinaryOperator.PLUS, new SingleLineTextRange(1, 8, 7, 1)),
                                        new IntegerLiteralExpressionNode("1", new SingleLineTextRange(1, 10, 9, 1)),
                                        new SingleLineTextRange(1, 5, 4, 6)),
                                new SingleLineTextRange(1, 1, 0, 11))),
                new SingleLineTextRange(1, 1, 0, 11)));
    }

    @Test
    public void ifStatementTest1() {
        var result = parse("if (a) b = 3;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(
                        new IfStatementNode(
                                new NameExpressionNode("a", new SingleLineTextRange(1, 5, 4, 1)),
                                new AssignmentStatementNode(
                                        new NameExpressionNode("b", new SingleLineTextRange(1, 8, 7, 1)),
                                        new AssignmentOperatorNode(AssignmentOperator.ASSIGNMENT, new SingleLineTextRange(1, 10, 9, 1)),
                                        new IntegerLiteralExpressionNode("3", new SingleLineTextRange(1, 12, 11, 1)),
                                        new SingleLineTextRange(1, 8, 7, 6)),
                                null,
                                new SingleLineTextRange(1, 1, 0, 13))),
                new SingleLineTextRange(1, 1, 0, 13)));
    }

    @Test
    public void ifStatementTest2() {
        var result = parse("if (a) b = 3;else{}");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(
                        new IfStatementNode(
                                new NameExpressionNode("a", new SingleLineTextRange(1, 5, 4, 1)),
                                new AssignmentStatementNode(
                                        new NameExpressionNode("b", new SingleLineTextRange(1, 8, 7, 1)),
                                        new AssignmentOperatorNode(AssignmentOperator.ASSIGNMENT, new SingleLineTextRange(1, 10, 9, 1)),
                                        new IntegerLiteralExpressionNode("3", new SingleLineTextRange(1, 12, 11, 1)),
                                        new SingleLineTextRange(1, 8, 7, 6)),
                                new BlockStatementNode(List.of(), new SingleLineTextRange(1, 18, 17, 2)),
                                new SingleLineTextRange(1, 1, 0, 19))),
                new SingleLineTextRange(1, 1, 0, 19)));
    }

    @Test
    public void returnStatementTest1() {
        var result = parse("return;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(
                        new ReturnStatementNode(null, new SingleLineTextRange(1, 1, 0, 7))),
                new SingleLineTextRange(1, 1, 0, 7)));
    }

    @Test
    public void returnStatementTest2() {
        var result = parse("return true;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(
                        new ReturnStatementNode(
                                new BooleanLiteralExpressionNode(true, new SingleLineTextRange(1, 8, 7, 4)),
                                new SingleLineTextRange(1, 1, 0, 12))),
                new SingleLineTextRange(1, 1, 0, 12)));
    }

    @Test
    public void lambdaTest1() {
        var result = parse("func(() => 1);");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(
                        new ExpressionStatementNode(
                                new InvocationExpressionNode(
                                        new NameExpressionNode("func", new SingleLineTextRange(1, 1, 0, 4)),
                                        new ArgumentsListNode(List.of(
                                                new LambdaExpressionNode(
                                                        List.of(),
                                                        new ExpressionStatementNode(
                                                                new IntegerLiteralExpressionNode("1", new SingleLineTextRange(1, 12, 11, 1)),
                                                                new SingleLineTextRange(1, 12, 11, 1)),
                                                        new SingleLineTextRange(1, 6, 5, 7))),
                                                new SingleLineTextRange(1, 5, 4, 9)),
                                        new SingleLineTextRange(1, 1, 0, 13)),
                                new SingleLineTextRange(1, 1, 0, 14))),
                new SingleLineTextRange(1, 1, 0, 14)));
    }

    @Test
    public void lambdaTest2() {
        var result = parse("func(a => {});");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(
                        new ExpressionStatementNode(
                                new InvocationExpressionNode(
                                        new NameExpressionNode("func", new SingleLineTextRange(1, 1, 0, 4)),
                                        new ArgumentsListNode(List.of(
                                                new LambdaExpressionNode(
                                                        List.of(
                                                                new NameExpressionNode(
                                                                    new IdentifierToken("a", new SingleLineTextRange(1, 6, 5, 1)))),
                                                        new BlockStatementNode(
                                                                List.of(),
                                                                new SingleLineTextRange(1, 11, 10, 2)),
                                                        new SingleLineTextRange(1, 6, 5, 7))),
                                                new SingleLineTextRange(1, 5, 4, 9)),
                                        new SingleLineTextRange(1, 1, 0, 13)),
                                new SingleLineTextRange(1, 1, 0, 14))),
                new SingleLineTextRange(1, 1, 0, 14)));
    }

    @Test
    public void lambdaTest3() {
        var result = parse("func((a, b, c) => {});");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(
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
                                                        new BlockStatementNode(
                                                                List.of(),
                                                                new SingleLineTextRange(1, 19, 18, 2)),
                                                        new SingleLineTextRange(1, 6, 5, 15))),
                                                new SingleLineTextRange(1, 5, 4, 17)),
                                        new SingleLineTextRange(1, 1, 0, 21)),
                                new SingleLineTextRange(1, 1, 0, 22))),
                new SingleLineTextRange(1, 1, 0, 22)));
    }

    @Test
    public void functionTest1() {
        var result = parse("void a(){}");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(),
                List.of(
                        new FunctionNode(
                                new VoidTypeNode(new SingleLineTextRange(1, 1, 0, 4)),
                                new NameExpressionNode(
                                        new IdentifierToken("a", new SingleLineTextRange(1, 6, 5, 1))),
                                new ParameterListNode(List.of(), new SingleLineTextRange(1, 7, 6, 2)),
                                new BlockStatementNode(List.of(), new SingleLineTextRange(1, 9, 8, 2)),
                                new SingleLineTextRange(1, 1, 0, 10))),
                List.of(),
                new SingleLineTextRange(1, 1, 0, 10)));
    }

    @Test
    public void functionTest2() {
        var result = parse("int[][][] a(int[][][] b, string s) {}");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(),
                List.of(
                        new FunctionNode(
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
                                        new SingleLineTextRange(1, 12, 11, 23)),
                                new BlockStatementNode(List.of(), new SingleLineTextRange(1, 36, 35, 2)),
                                new SingleLineTextRange(1, 1, 0, 37))),
                List.of(),
                new SingleLineTextRange(1, 1, 0, 37)));
    }

    private ParserOutput parse(String code) {
        return new Parser(new Lexer(new LexerInput(code)).lex()).parse();
    }
}