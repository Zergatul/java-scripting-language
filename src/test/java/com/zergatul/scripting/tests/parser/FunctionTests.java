package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.lexer.Trivia;
import com.zergatul.scripting.lexer.ValueToken;
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
        comparator.assertEquals(new CompilationUnitNode(
                new CompilationUnitMembersListNode(List.of(
                        new FunctionNode(
                                new ModifiersNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                                new VoidTypeNode(new SingleLineTextRange(1, 1, 0, 4)),
                                new NameExpressionNode(
                                        new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 6, 5, 1))),
                                new ParameterListNode(
                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 7, 6, 1)),
                                        List.of(),
                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 8, 7, 1)),
                                        new SingleLineTextRange(1, 7, 6, 2)),
                                new BlockStatementNode(List.of(), new SingleLineTextRange(1, 9, 8, 2)),
                                new SingleLineTextRange(1, 1, 0, 10))),
                        new SingleLineTextRange(1, 1, 0, 10)),
                new StatementsListNode(List.of(), new SingleLineTextRange(1, 11, 10, 0)),
                new SingleLineTextRange(1, 1, 0, 10)),
                result.unit());
    }

    @Test
    public void functionTest2() {
        ParserOutput result = parse("int[][][] a(int[][][] b, string s) {}");
        comparator.assertEquals(List.of(), result.diagnostics());
        comparator.assertEquals(new CompilationUnitNode(
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
                                        new ValueToken(TokenType.IDENTIFIER, "a", new SingleLineTextRange(1, 11, 10, 1))),
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
                                                                new ValueToken(TokenType.IDENTIFIER, "b", new SingleLineTextRange(1, 23, 22, 1))),
                                                        new SingleLineTextRange(1, 13, 12, 11)),
                                                new ParameterNode(
                                                        new PredefinedTypeNode(PredefinedType.STRING, new SingleLineTextRange(1, 26, 25, 6)),
                                                        new NameExpressionNode(
                                                                new ValueToken(TokenType.IDENTIFIER, "s", new SingleLineTextRange(1, 33, 32, 1))),
                                                        new SingleLineTextRange(1, 26, 25, 8))),
                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 34, 33, 1))
                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 35, 34, 1))),
                                        new SingleLineTextRange(1, 12, 11, 23)),
                                new BlockStatementNode(List.of(), new SingleLineTextRange(1, 36, 35, 2)),
                                new SingleLineTextRange(1, 1, 0, 37))),
                        new SingleLineTextRange(1, 1, 0, 37)),
                new StatementsListNode(List.of(), new SingleLineTextRange(1, 38, 37, 0)),
                new SingleLineTextRange(1, 1, 0, 37)),
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