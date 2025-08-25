package com.zergatul.scripting.tests;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.parser.*;
import com.zergatul.scripting.symbols.Function;
import com.zergatul.scripting.symbols.ImmutableSymbolRef;
import com.zergatul.scripting.symbols.LocalVariable;
import com.zergatul.scripting.symbols.StaticFieldConstantStaticVariable;
import com.zergatul.scripting.tests.compiler.helpers.FutureHelper;
import com.zergatul.scripting.tests.compiler.helpers.Run;
import com.zergatul.scripting.type.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.assertDiagnostic;

public class ErrorRecoveryBinderTests {

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

        assertDiagnostic(result.diagnostics(),
                new DiagnosticMessage(BinderErrors.NotFunction, new SingleLineTextRange(1, 11, 10, 8), NodeType.STRING_LITERAL));
    }

    @Test
    public void unknownFieldMethodTest() {
        BinderOutput result = bind("""
                futures.
                run.once(() => {});
                """);
        Assertions.assertFalse(result.diagnostics().isEmpty());
        Assertions.assertIterableEquals(result.diagnostics(),
                List.of(
                        new DiagnosticMessage(
                                BinderErrors.MemberDoesNotExist,
                                new SingleLineTextRange(2, 1, 9, 3),
                                "Java<com.zergatul.scripting.tests.compiler.helpers.FutureHelper>",
                                "run")));
    }

    @Test
    public void unfinishedReturnStatementTest() {
        BinderOutput result = bind("""
                int func() {
                    return ;
                }
                """);

        Assertions.assertIterableEquals(
                result.diagnostics(),
                List.of(
                        new DiagnosticMessage(BinderErrors.EmptyReturnStatement, new SingleLineTextRange(2, 5, 17, 8), "void")));

        Assertions.assertEquals(
                result.unit().members,
                new BoundCompilationUnitMembersListNode(
                        List.of(
                                new BoundFunctionNode(
                                        false,
                                        new BoundPredefinedTypeNode(SInt.instance, new SingleLineTextRange(1, 1, 0, 3)),
                                        new BoundNameExpressionNode(
                                                new Function("func", new SStaticFunction(SInt.instance, new MethodParameter[0]), new MultiLineTextRange(1, 1, 3, 2, 0, 27)),
                                                new SingleLineTextRange(1, 5, 4, 4)),
                                        new BoundParameterListNode(List.of(), new SingleLineTextRange(1, 9, 8, 2)),
                                        new BoundBlockStatementNode(
                                                List.of(
                                                        new BoundReturnStatementNode(
                                                                new Token(TokenType.RETURN, new SingleLineTextRange(2, 11, 23, 6)),
                                                                new BoundInvalidExpressionNode(List.of(), new SingleLineTextRange(2, 11, 23, 2)),
                                                                new SingleLineTextRange(2, 5, 17, 8))),
                                                new MultiLineTextRange(1, 12, 3, 2, 11, 16)),
                                        List.of(),
                                        new MultiLineTextRange(1, 1, 3, 2, 0, 27))),
                        new MultiLineTextRange(1, 1, 3, 2, 0, 27)));
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

        Assertions.assertIterableEquals(
                result.diagnostics(),
                List.of(new DiagnosticMessage(ParserErrors.SimpleStatementExpected, new SingleLineTextRange(1, 21, 20, 1), ")")));

        Assertions.assertEquals(
                result.unit().statements,
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
                                                new BoundArgumentsListNode(List.of(
                                                        new BoundLambdaExpressionNode(
                                                                SFunctionalInterface.from((ParameterizedType) Run.class.getMethod("onString", Run.Action1.class).getGenericParameterTypes()[0]),
                                                                List.of(new BoundParameterNode(
                                                                        new BoundNameExpressionNode(
                                                                                new LocalVariable("str", SString.instance, new SingleLineTextRange(1, 14, 13, 3)),
                                                                                new SingleLineTextRange(1, 14, 13, 3)),
                                                                        SString.instance,
                                                                        new SingleLineTextRange(1, 14, 13, 3))),
                                                                new BoundInvalidStatementNode(new SingleLineTextRange(1, 21, 20, 0)),
                                                                List.of(),
                                                                List.of(),
                                                                new SingleLineTextRange(1, 14, 13, 7))),
                                                        new SingleLineTextRange(1, 13, 12, 9)),
                                                List.of(),
                                                new SingleLineTextRange(1, 1, 0, 21)),
                                        new SingleLineTextRange(1, 1, 0, 22))),
                        List.of(),
                        new SingleLineTextRange(1, 1, 0, 22)));
    }

    private BinderOutput bind(String code) {
        CompilationParameters parameters = new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .build();
        return new Binder(new Parser(new Lexer(new LexerInput(code)).lex()).parse(), parameters).bind();
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