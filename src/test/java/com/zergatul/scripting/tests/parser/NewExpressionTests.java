package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.lexer.Trivia;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.PredefinedType;
import com.zergatul.scripting.parser.nodes.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class NewExpressionTests extends ParserTestBase {

    @Test
    public void newExpressionTest1() {
        ParserOutput result = parse("""
                let x = new int[10];
                """);
        comparator.assertEquals(List.of(), result.diagnostics());
//        comparator.assertEquals(new CompilationUnitNode(
//                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
//                new StatementsListNode(List.of(
//                        new VariableDeclarationNode(
//                                new LetTypeNode(new SingleLineTextRange(1, 1, 0, 3)),
//                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
//                                new ArrayCreationExpressionNode(
//                                        new Token(TokenType.NEW, new SingleLineTextRange(1, 9, 8, 3))
//                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 12, 11, 1))),
//                                        new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 13, 12, 3)),
//                                        new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(1, 16, 15, 1)),
//                                        new IntegerLiteralExpressionNode("10", new SingleLineTextRange(1, 17, 16, 2)),
//                                        new Token(TokenType.RIGHT_SQUARE_BRACKET, new SingleLineTextRange(1, 19, 18, 1)),
//                                        new SingleLineTextRange(1, 9, 8, 11)),
//                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 20, 19, 1))
//                                        .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(1, 21, 20, 1))),
//                                new SingleLineTextRange(1, 1, 0, 20))),
//                        new SingleLineTextRange(1, 1, 0, 20)),
//                new SingleLineTextRange(1, 1, 0, 20)),
//                result.unit());
    }

    @Test
    public void newExpressionTest2() {
        ParserOutput result = parse("""
                let x = new int[] { 1, 2, 3 };
                """);
        comparator.assertEquals(List.of(), result.diagnostics());
//        comparator.assertEquals(new CompilationUnitNode(
//                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
//                new StatementsListNode(List.of(
//                        new VariableDeclarationNode(
//                                new LetTypeNode(new SingleLineTextRange(1, 1, 0, 3)),
//                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
//                                new ArrayInitializerExpressionNode(
//                                        new Token(TokenType.NEW, new SingleLineTextRange(1, 9, 8, 3))
//                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 12, 11, 1))),
//                                        new ArrayTypeNode(
//                                                new PredefinedTypeNode(PredefinedType.INT, new SingleLineTextRange(1, 13, 12, 3)),
//                                                new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(1, 16, 15, 1)),
//                                                new Token(TokenType.RIGHT_SQUARE_BRACKET, new SingleLineTextRange(1, 17, 16, 1))
//                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 18, 17, 1))),
//                                                new SingleLineTextRange(1, 13, 12, 5)),
//                                        new Token(TokenType.LEFT_CURLY_BRACKET, new SingleLineTextRange(1, 19, 18, 1))
//                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 20, 19, 1))),
//                                        List.of(
//                                                new IntegerLiteralExpressionNode("1", new SingleLineTextRange(1, 21, 20, 1)),
//                                                new IntegerLiteralExpressionNode("2", new SingleLineTextRange(1, 24, 23, 1)),
//                                                new IntegerLiteralExpressionNode("3", new SingleLineTextRange(1, 27, 26, 1))),
//                                        new Token(TokenType.RIGHT_CURLY_BRACKET, new SingleLineTextRange(1, 29, 28, 1)),
//                                        new SingleLineTextRange(1, 9, 8, 21)),
//                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 30, 29, 1))
//                                        .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(1, 31, 30, 1))),
//                                new SingleLineTextRange(1, 1, 0, 30))),
//                        new SingleLineTextRange(1, 1, 0, 30)),
//                new SingleLineTextRange(1, 1, 0, 30)),
//                result.unit());
    }

    @Test
    public void newExpressionTest3() {
        ParserOutput result = parse("""
                let x = new ClassA();
                """);
        comparator.assertEquals(List.of(), result.diagnostics());
//        comparator.assertEquals(new CompilationUnitNode(
//                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
//                new StatementsListNode(List.of(
//                        new VariableDeclarationNode(new LetTypeNode(new SingleLineTextRange(1, 1, 0, 3)),
//                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
//                                new ObjectCreationExpressionNode(
//                                        new CustomTypeNode("ClassA", new SingleLineTextRange(1, 13, 12, 6)),
//                                        new ArgumentsListNode(
//                                                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 19, 18, 1)),
//                                                List.of(),
//                                                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 20, 19, 1)),
//                                                new SingleLineTextRange(1, 19, 18, 2)),
//                                        new SingleLineTextRange(1, 9, 8, 12)),
//                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 21, 20, 1))
//                                        .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(1, 22, 21, 1))),
//                                new SingleLineTextRange(1, 1, 0, 21))),
//                        new SingleLineTextRange(1, 1, 0, 21)),
//                new SingleLineTextRange(1, 1, 0, 21)),
//                result.unit());
    }
}