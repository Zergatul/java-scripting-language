package com.zergatul.scripting.tests;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserErrors;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.PredefinedType;
import com.zergatul.scripting.parser.nodes.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ErrorRecoveryParserTests {

    @Test
    public void unfinishedMemberAccess1Test() {
        var result = parse("""
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
        var result = parse("""
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
        var result = parse("""
                { *
                """);
        Assertions.assertFalse(result.diagnostics().isEmpty());
    }

    @Test
    public void missingArgumentTest() {
        var result = parse("""
                main.chat("abc",);
                """);
        Assertions.assertFalse(result.diagnostics().isEmpty());
    }

    private ParserOutput parse(String code) {
        return new Parser(new Lexer(new LexerInput(code)).lex()).parse();
    }
}