package com.zergatul.scripting.tests;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderErrors;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.Parser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ErrorRecoveryBinderTests {

    @Test
    public void missingArgumentTest() {
        var result = bind("""
                main.chat("abc",);
                """);
        Assertions.assertFalse(result.diagnostics().isEmpty());
    }

    @Test
    public void invalidMethodCallTest() {
        var result = bind("""
                main.chat("Hello!"(
                """);
        Assertions.assertFalse(result.diagnostics().isEmpty());
        var option = result.diagnostics().stream().filter(d -> d.code.equals(BinderErrors.InvalidCallee.code())).findFirst();
        Assertions.assertTrue(option.isPresent());
        Assertions.assertEquals(option.get(), new DiagnosticMessage(BinderErrors.InvalidCallee, new SingleLineTextRange(1, 11, 10, 8), NodeType.STRING_LITERAL));
    }

    private BinderOutput bind(String code) {
        CompilationParameters parameters = new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .build();
        return new Binder(new Parser(new Lexer(new LexerInput(code)).lex()).parse(), parameters).bind();
    }

    public static class ApiRoot {
        public static final Main main = new Main();
    }

    public static class Main {
        public void chat(String message) {}
    }
}