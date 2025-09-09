package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.ParserErrors;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.PredefinedType;
import com.zergatul.scripting.parser.nodes.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class FunctionTests extends ParserTestBase {

    @Test
    public void functionTest1() {
        ParserOutput result = parse("void a(){}");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(
                                new FunctionNode(
                                        new ModifiersNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                                        new VoidTypeNode(
                                                new Token(TokenType.VOID, new SingleLineTextRange(1, 1, 0, 4))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 5, 4, 1)))),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 6, 5, 1))),
                                        new ParameterListNode(
                                                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 7, 6, 1)),
                                                SeparatedList.of(),
                                                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 8, 7, 1))),
                                        null,
                                        new BlockStatementNode(
                                                new Token(TokenType.LEFT_CURLY_BRACKET, new SingleLineTextRange(1, 9, 8, 1)),
                                                List.of(),
                                                new Token(TokenType.RIGHT_CURLY_BRACKET, new SingleLineTextRange(1, 10, 9, 1))))),
                                new SingleLineTextRange(1, 1, 0, 10)),
                        new StatementsListNode(List.of(), new SingleLineTextRange(1, 11, 10, 0)),
                        new EndOfFileToken(new SingleLineTextRange(1, 11, 10, 0))),
                result.unit());
    }

    @Test
    public void functionTest2() {
        ParserOutput result = parse("int[][][] a(int[][][] b, string s) {}");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(
                new CompilationUnitNode(
                        new CompilationUnitMembersListNode(List.of(
                                new FunctionNode(
                                        new ModifiersNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                                        new ArrayTypeNode(
                                                new ArrayTypeNode(
                                                        new ArrayTypeNode(
                                                                new PredefinedTypeNode(
                                                                        new Token(TokenType.INT, new SingleLineTextRange(1, 1, 0, 3)),
                                                                        PredefinedType.INT),
                                                                new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(1, 4, 3, 1)),
                                                                new Token(TokenType.RIGHT_SQUARE_BRACKET, new SingleLineTextRange(1, 5, 4, 1)),
                                                                new SingleLineTextRange(1, 1, 0, 5)),
                                                        new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(1, 6, 5, 1)),
                                                        new Token(TokenType.RIGHT_SQUARE_BRACKET, new SingleLineTextRange(1, 7, 6, 1)),
                                                        new SingleLineTextRange(1, 1, 0, 7)),
                                                new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(1, 8, 7, 1)),
                                                new Token(TokenType.RIGHT_SQUARE_BRACKET, new SingleLineTextRange(1, 9, 8, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 10, 9, 1))),
                                                new SingleLineTextRange(1, 1, 0, 9)),
                                        new NameExpressionNode(
                                                new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 11, 10, 1))),
                                        new ParameterListNode(
                                                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 12, 11, 1)),
                                                SeparatedList.of(
                                                        ParameterNode.class,
                                                        new ParameterNode(
                                                                new ArrayTypeNode(
                                                                        new ArrayTypeNode(
                                                                                new ArrayTypeNode(
                                                                                        new PredefinedTypeNode(
                                                                                                new Token(TokenType.INT, new SingleLineTextRange(1, 13, 12, 3)),
                                                                                                PredefinedType.INT),
                                                                                        new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(1, 16, 15, 1)),
                                                                                        new Token(TokenType.RIGHT_SQUARE_BRACKET, new SingleLineTextRange(1, 17, 16, 1)),
                                                                                        new SingleLineTextRange(1, 13, 12, 5)),
                                                                                new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(1, 18, 17, 1)),
                                                                                new Token(TokenType.RIGHT_SQUARE_BRACKET, new SingleLineTextRange(1, 19, 18, 1)),
                                                                                new SingleLineTextRange(1, 13, 12, 7)),
                                                                        new Token(TokenType.LEFT_SQUARE_BRACKET, new SingleLineTextRange(1, 20, 19, 1)),
                                                                        new Token(TokenType.RIGHT_SQUARE_BRACKET, new SingleLineTextRange(1, 21, 20, 1))
                                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 22, 21, 1))),
                                                                        new SingleLineTextRange(1, 13, 12, 9)),
                                                                new NameExpressionNode(
                                                                        new ValueToken(TokenType.IDENTIFIER, "b", new SingleLineTextRange(1, 23, 22, 1)))),
                                                        new Token(TokenType.COMMA, new SingleLineTextRange(1, 24, 23, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 25, 24, 1))),
                                                        new ParameterNode(
                                                                new PredefinedTypeNode(
                                                                        new Token(TokenType.STRING, new SingleLineTextRange(1, 26, 25, 6))
                                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 32, 31, 1))),
                                                                        PredefinedType.STRING),
                                                                new NameExpressionNode(
                                                                        new ValueToken(TokenType.IDENTIFIER, "s", new SingleLineTextRange(1, 33, 32, 1))))),
                                                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 34, 33, 1))
                                                        .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 35, 34, 1)))),
                                        null,
                                        new BlockStatementNode(
                                                new Token(TokenType.LEFT_CURLY_BRACKET, new SingleLineTextRange(1, 36, 35, 1)),
                                                List.of(),
                                                new Token(TokenType.RIGHT_CURLY_BRACKET, new SingleLineTextRange(1, 37, 36, 1))))),
                                new SingleLineTextRange(1, 1, 0, 37)),
                        new StatementsListNode(List.of(), new SingleLineTextRange(1, 38, 37, 0)),
                        new EndOfFileToken(new SingleLineTextRange(1, 38, 37, 0))),
                result.unit());
    }

    @Test
    public void functionAfterVariableTest() {
        ParserOutput result = parse("""
                int x = 0;
                int func()
                """);
        comparator.assertEquals(List.of(
                new DiagnosticMessage(
                        ParserErrors.SemicolonOrEqualExpected,
                        new SingleLineTextRange(2, 9, 19, 1),
                        "(")),
                result.diagnostics().stream().limit(1).toList());
    }
}