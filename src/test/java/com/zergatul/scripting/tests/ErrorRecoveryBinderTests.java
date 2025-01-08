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
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.symbols.Function;
import com.zergatul.scripting.tests.compiler.helpers.FutureHelper;
import com.zergatul.scripting.tests.compiler.helpers.Run;
import com.zergatul.scripting.type.MethodParameter;
import com.zergatul.scripting.type.SFunction;
import com.zergatul.scripting.type.SInt;
import com.zergatul.scripting.type.SType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        Assertions.assertFalse(result.diagnostics().isEmpty());
        var option = result.diagnostics().stream().filter(d -> d.code.equals(BinderErrors.InvalidCallee.code())).findFirst();
        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals(option.get(), new DiagnosticMessage(BinderErrors.InvalidCallee, new SingleLineTextRange(1, 11, 10, 8), NodeType.STRING_LITERAL));
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
                result.unit().functions,
                new BoundFunctionsListNode(
                        List.of(
                                new BoundFunctionNode(
                                        false,
                                        new BoundPredefinedTypeNode(SInt.instance, new SingleLineTextRange(1, 1, 0, 3)),
                                        new BoundNameExpressionNode(
                                                new Function("func", new SFunction(SInt.instance, new MethodParameter[0]), new MultiLineTextRange(1, 1, 3, 2, 0, 27)),
                                                new SingleLineTextRange(1, 5, 4, 4)),
                                        new BoundParameterListNode(List.of(), new SingleLineTextRange(1, 9, 8, 2)),
                                        new BoundBlockStatementNode(
                                                List.of(
                                                        new BoundReturnStatementNode(
                                                                new BoundInvalidExpressionNode(new SingleLineTextRange(2, 11, 23, 2)),
                                                                new SingleLineTextRange(2, 5, 17, 8))),
                                                new MultiLineTextRange(1, 12, 3, 2, 11, 16)),
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