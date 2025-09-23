package com.zergatul.scripting.tests.binder;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.binding.nodes.BoundNodeType;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.*;
import com.zergatul.scripting.parser.nodes.ModifiersNode;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.tests.compiler.helpers.FutureHelper;
import com.zergatul.scripting.tests.compiler.helpers.Run;
import com.zergatul.scripting.type.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.util.List;

public class ErrorRecoveryTests extends BinderTestBase {

    @Test
    public void missingArgumentTest() {
        BinderOutput result = bind("""
                main.chat("abc",);
                """);
        Assertions.assertFalse(result.diagnostics().isEmpty());
    }

    @Test
    public void invalidMethodCallTest() {
        BinderOutput result = bind("""
                main.chat("Hello!"(
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(BinderErrors.NotFunction, new SingleLineTextRange(1, 11, 10, 8), BoundNodeType.STRING_LITERAL)),
                result.diagnostics().reversed().stream().limit(1).toList());
    }

    @Test
    public void unknownFieldMethodTest() {
        BinderOutput result = bind("""
                futures.
                run.once(() => {});
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(
                        BinderErrors.MemberDoesNotExist,
                        new SingleLineTextRange(2, 1, 9, 3),
                        "Java<com.zergatul.scripting.tests.compiler.helpers.FutureHelper>",
                        "run")),
                result.diagnostics());
    }

    @Test
    public void unfinishedReturnStatementTest() {
        BinderOutput result = bind("""
                int func() {
                    return ;
                }
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(BinderErrors.EmptyReturnStatement, new SingleLineTextRange(2, 5, 17, 8), "void")),
                result.diagnostics());
        comparator.assertEquals(
                new BoundCompilationUnitNode(
                        new BoundCompilationUnitMembersListNode(
                                List.of(
                                        new BoundFunctionNode(
                                                new ModifiersNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                                                false,
                                                new BoundPredefinedTypeNode(SInt.instance, new SingleLineTextRange(1, 1, 0, 3)),
                                                new BoundNameExpressionNode(
                                                        new ImmutableSymbolRef(
                                                                new Function("func", new SStaticFunction(SInt.instance, new MethodParameter[0]), new MultiLineTextRange(1, 1, 3, 2, 0, 27))),
                                                        new SingleLineTextRange(1, 5, 4, 4)),
                                                new BoundParameterListNode(
                                                        new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 9, 8, 1)),
                                                        List.of(),
                                                        new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 10, 9, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 11, 10, 1))),
                                                        new SingleLineTextRange(1, 9, 8, 2)),
                                                new BoundBlockStatementNode(
                                                        new Token(TokenType.LEFT_CURLY_BRACKET, new SingleLineTextRange(1, 12, 11, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(1, 13, 12, 1))),
                                                        List.of(
                                                                new BoundReturnStatementNode(
                                                                        new Token(TokenType.RETURN, new SingleLineTextRange(2, 5, 17, 6))
                                                                                .withLeadingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 1, 13, 4)))
                                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(2, 11, 23, 1))),
                                                                        new BoundInvalidExpressionNode(List.of(), new SingleLineTextRange(2, 11, 23, 2)),
                                                                        new SingleLineTextRange(2, 5, 17, 8))),
                                                        new Token(TokenType.RIGHT_CURLY_BRACKET, new SingleLineTextRange(3, 1, 26, 1))
                                                                .withTrailingTrivia(new Trivia(TokenType.LINE_BREAK, new SingleLineTextRange(3, 2, 27, 1))),
                                                        new MultiLineTextRange(1, 12, 3, 2, 11, 16)),
                                                List.of(),
                                                new MultiLineTextRange(1, 1, 3, 2, 0, 27))),
                                new MultiLineTextRange(1, 1, 3, 2, 0, 27)),
                        new BoundStatementsListNode(List.of(), List.of(), new SingleLineTextRange(3, 2, 27, 0)),
                        new MultiLineTextRange(1, 1, 3, 2, 0, 27)),
                result.unit());
    }

    @Test
    public void unfinishedFunctionTest() {
        BinderOutput result = bind("""
                void
                let x = 1;
                """);

        Assertions.assertFalse(result.diagnostics().isEmpty());
    }

    @Test
    public void unfinishedLambdaTest() throws NoSuchFieldException, NoSuchMethodException {
        BinderOutput result = bind("""
                run.onString(str => );
                """);

        comparator.assertEquals(List.of(
                new DiagnosticMessage(ParserErrors.SimpleStatementExpected, new SingleLineTextRange(1, 21, 20, 1), ")")),
                result.diagnostics());
        comparator.assertEquals(
                new BoundCompilationUnitNode(
                        new BoundCompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),
                        new BoundStatementsListNode(
                                List.of(
                                        new BoundExpressionStatementNode(
                                                new BoundMethodInvocationExpressionNode(
                                                        new BoundNameExpressionNode(
                                                                new ImmutableSymbolRef(new StaticFieldConstantStaticVariable("run", ApiRoot.class.getField("run"))),
                                                                SType.fromJavaType(Run.class),
                                                                "run",
                                                                new SingleLineTextRange(1, 1, 0, 3)),
                                                        new Token(TokenType.DOT, new SingleLineTextRange(1, 4, 3, 1)),
                                                        new BoundMethodNode(
                                                                new NativeInstanceMethodReference(
                                                                        Run.class.getMethod("onString", Run.Action1.class)),
                                                                new SingleLineTextRange(1, 5, 4, 8)),
                                                        new BoundArgumentsListNode(
                                                                new Token(TokenType.LEFT_PARENTHESES, new SingleLineTextRange(1, 13, 12, 1)),
                                                                BoundSeparatedList.of(List.of(
                                                                new BoundLambdaExpressionNode(
                                                                        SFunctionalInterface.from((ParameterizedType) Run.class.getMethod("onString", Run.Action1.class).getGenericParameterTypes()[0]),
                                                                        List.of(new BoundParameterNode(
                                                                                new BoundNameExpressionNode(
                                                                                        new LocalVariable("str", SString.instance, new SingleLineTextRange(1, 14, 13, 3)),
                                                                                        new SingleLineTextRange(1, 14, 13, 3)),
                                                                                SString.instance,
                                                                                new SingleLineTextRange(1, 14, 13, 3))),
                                                                        new Token(TokenType.EQUAL_GREATER, new SingleLineTextRange(1, 18, 17, 2))
                                                                                .withTrailingTrivia(new Trivia(TokenType.WHITESPACE, new SingleLineTextRange(1, 20, 19, 1))),
                                                                        new BoundInvalidStatementNode(new SingleLineTextRange(1, 21, 20, 0)),
                                                                        List.of(),
                                                                        List.of(),
                                                                        new SingleLineTextRange(1, 14, 13, 7)))),
                                                                new Token(TokenType.RIGHT_PARENTHESES, new SingleLineTextRange(1, 21, 20, 1)),
                                                                new SingleLineTextRange(1, 13, 12, 9)),
                                                        List.of(),
                                                        new SingleLineTextRange(1, 1, 0, 21)),
                                                new SingleLineTextRange(1, 1, 0, 22))),
                                List.of(),
                                new SingleLineTextRange(1, 1, 0, 22)),
                        new SingleLineTextRange(1, 1, 0, 22)),
                result.unit());
    }

    private BinderOutput bind(String code) {
        return bind(ApiRoot.class, code);
    }

    public static class ApiRoot {
        public static final Main main = new Main();
        public static final Run run = new Run();
        public static final FutureHelper futures = new FutureHelper();
    }

    public static class Main {

        public void chat(String message) {}

        public int getInt() {
            return 123;
        }
    }
}