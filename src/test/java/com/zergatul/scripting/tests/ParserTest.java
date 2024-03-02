package com.zergatul.scripting.tests;

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
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(new ArrayList<>()));
    }

    @Test
    public void emptyBlockStatementTest() {
        var result = parse("{}");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(new BlockStatementNode(new ArrayList<>()))));
    }

    @Test
    public void simpleVariableDeclarationTest() {
        var result = parse("int x;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(new VariableDeclarationNode(
                        new PredefinedTypeNode(PredefinedType.INT),
                        "x"
                ))));
    }

    @Test
    public void simpleVariableDeclarationWithInitializerTest() {
        var result = parse("int x = 10;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(new VariableDeclarationNode(
                        new PredefinedTypeNode(PredefinedType.INT),
                        "x",
                        new IntegerLiteralExpressionNode("10")
                ))));
    }

    @Test
    public void arrayVariableDeclarationTest() {
        var result = parse("int[][] x;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(new VariableDeclarationNode(
                        new ArrayTypeNode(
                                new ArrayTypeNode(
                                    new PredefinedTypeNode(PredefinedType.INT))),
                        "x"
                ))));
    }

    @Test
    public void simpleAssignmentTest() {
        var result = parse("a = b + c;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(new AssignmentStatementNode(
                        new NameExpressionNode("a"),
                        AssignmentOperator.ASSIGNMENT,
                        new BinaryExpressionNode(
                                new NameExpressionNode("b"),
                                BinaryOperator.PLUS,
                                new NameExpressionNode("c")
                )))));
    }

    @Test
    public void assignmentOperatorsTest() {
        var result = parse("a += 10; b -= 15; c *= 2; d /= 2;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(List.of(
                new AssignmentStatementNode(
                        new NameExpressionNode("a"),
                        AssignmentOperator.PLUS_ASSIGNMENT,
                        new IntegerLiteralExpressionNode("10")),
                new AssignmentStatementNode(
                        new NameExpressionNode("b"),
                        AssignmentOperator.MINUS_ASSIGNMENT,
                        new IntegerLiteralExpressionNode("15")),
                new AssignmentStatementNode(
                        new NameExpressionNode("c"),
                        AssignmentOperator.MULTIPLY_ASSIGNMENT,
                        new IntegerLiteralExpressionNode("2")),
                new AssignmentStatementNode(
                        new NameExpressionNode("d"),
                        AssignmentOperator.DIVIDE_ASSIGNMENT,
                        new IntegerLiteralExpressionNode("2")))));
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
                                            new NameExpressionNode("abc"),
                                            "qwe"),
                                    "x"),
                            new ArrayList<>())))));
    }

    @Test
    public void negativeIntegerTest() {
        var result = parse("x = -100;");
        Assertions.assertEquals(result.diagnostics().size(), 0);
        Assertions.assertEquals(result.unit(), new CompilationUnitNode(
                List.of(new AssignmentStatementNode(
                        new NameExpressionNode("x"),
                        AssignmentOperator.ASSIGNMENT,
                        new IntegerLiteralExpressionNode("-100")))));
    }

    private ParserOutput parse(String code) {
        return new Parser(new Lexer(new LexerInput(code)).lex()).parse();
    }
}