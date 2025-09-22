package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.lexer.Trivia;
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
//        comparator.assertEquals(new CompilationUnitNode(
//                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
//                new StatementsListNode(List.of(
//                        new VariableDeclarationNode(
//                                new LetTypeNode(new SingleLineTextRange(1, 1, 0, 3)),
//                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
//                                new BinaryExpressionNode(
//                                        new TypeTestExpressionNode(
//                                                new NameExpressionNode("a", new SingleLineTextRange(1, 9, 8, 1)),
//                                                new PredefinedTypeNode(PredefinedType.STRING, new SingleLineTextRange(1, 14, 13, 6)),
//                                                new SingleLineTextRange(1, 9, 8, 11)),
//                                        new BinaryOperatorNode(BinaryOperator.BOOLEAN_OR, new SingleLineTextRange(1, 21, 20, 2)),
//                                        new NameExpressionNode("b", new SingleLineTextRange(1, 24, 23, 1)),
//                                        new SingleLineTextRange(1, 9, 8, 16)),
//                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 25, 24, 1))
//                                        .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(1, 26, 25, 1))),
//                                new SingleLineTextRange(1, 1, 0, 25))),
//                        new SingleLineTextRange(1, 1, 0, 25)),
//                new SingleLineTextRange(1, 1, 0, 25)),
//                result.unit());
    }

    @Test
    public void typeTestExpressionTest2() {
        ParserOutput result = parse("""
                let x = a == b is string;
                """);
        comparator.assertEquals(List.of(), result.diagnostics());
//        comparator.assertEquals(new CompilationUnitNode(
//                new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
//                new StatementsListNode(List.of(
//                        new VariableDeclarationNode(
//                                new LetTypeNode(new SingleLineTextRange(1, 1, 0, 3)),
//                                new NameExpressionNode("x", new SingleLineTextRange(1, 5, 4, 1)),
//                                new BinaryExpressionNode(
//                                        new NameExpressionNode("a", new SingleLineTextRange(1, 9, 8, 1)),
//                                        new BinaryOperatorNode(BinaryOperator.EQUALS, new SingleLineTextRange(1, 11, 10, 2)),
//                                        new TypeTestExpressionNode(
//                                                new NameExpressionNode("b", new SingleLineTextRange(1, 14, 13, 1)),
//                                                new PredefinedTypeNode(PredefinedType.STRING, new SingleLineTextRange(1, 19, 18, 6)),
//                                                new SingleLineTextRange(1, 14, 13, 11)),
//                                        new SingleLineTextRange(1, 9, 8, 16)),
//                                new Token(TokenType.SEMICOLON, new SingleLineTextRange(1, 25, 24, 1))
//                                        .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(1, 26, 25, 1))),
//                                new SingleLineTextRange(1, 1, 0, 25))),
//                        new SingleLineTextRange(1, 1, 0, 25)),
//                new SingleLineTextRange(1, 1, 0, 25)),
//                result.unit());
    }
}