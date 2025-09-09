package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.PredefinedType;
import com.zergatul.scripting.parser.nodes.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TypeTestExpressionTests extends ParserTestBase {

    @Test
    public void typeTestExpressionTest1() {
        ParserOutput result = parse("""
                let x = a is string || b;
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
                                                new TypeTestExpressionNode(
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 9, 8, 1))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 10, 9, 1)))),
                                                        new Token(TokenType.IS, new SingleLineTextRange(1, 11, 10, 2))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 13, 12, 1))),
                                                        new PredefinedTypeNode(
                                                                new Token(TokenType.STRING, new SingleLineTextRange(1, 14, 13, 6))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 20, 19, 1))),
                                                                PredefinedType.STRING)),
                                                new BinaryOperatorNode(
                                                        new Token(TokenType.PIPE_PIPE, new SingleLineTextRange(1, 21, 20, 2))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 23, 22, 1))),
                                                        BinaryOperator.BOOLEAN_OR),
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "b", new SingleLineTextRange(1, 24, 23, 1))),
                                                new SingleLineTextRange(1, 9, 8, 16)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 25, 24, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 26, 2, 1, 25, 1))))),
                                new SingleLineTextRange(1, 1, 0, 25)),
                        new EndOfFileToken(new SingleLineTextRange(2, 1, 26, 0))),
                result.unit());
    }

    @Test
    public void typeTestExpressionTest2() {
        ParserOutput result = parse("""
                let x = a == b is string;
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
                                                new NameExpressionNode(
                                                        new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 9, 8, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 10, 9, 1)))),
                                                new BinaryOperatorNode(
                                                        new Token(TokenType.EQUAL_EQUAL, new SingleLineTextRange(1, 11, 10, 2))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 13, 12, 1))),
                                                        BinaryOperator.EQUALS),
                                                new TypeTestExpressionNode(
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "b", new SingleLineTextRange(1, 14, 13, 1))
                                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 15, 14, 1)))),
                                                        new Token(TokenType.IS, new SingleLineTextRange(1, 16, 15, 2))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 18, 17, 1))),
                                                        new PredefinedTypeNode(
                                                                new Token(TokenType.STRING, new SingleLineTextRange(1, 19, 18, 6)),
                                                                PredefinedType.STRING)),
                                                new SingleLineTextRange(1, 9, 8, 16)),
                                        new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 25, 24, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new MultiLineTextRange(1, 26, 2, 1, 25, 1))))),
                                new SingleLineTextRange(1, 1, 0, 25)),
                        new EndOfFileToken(new SingleLineTextRange(2, 1, 26, 0))),
                result.unit());
    }
}