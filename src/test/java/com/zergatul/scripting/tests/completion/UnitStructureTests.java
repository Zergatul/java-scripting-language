package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.completion.CompletionProvider;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.symbols.StaticFieldConstantStaticVariable;
import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class UnitStructureTests {

    @Test
    public void emptyFileTest() throws NoSuchFieldException {
        assertSuggestions("", 1, 1, List.of(
                new StaticKeywordSuggestion(),
                new VoidKeywordSuggestion(),
                new LetKeywordSuggestion(),
                new TypeSuggestion(SBoolean.instance),
                new TypeSuggestion(SInt.instance),
                new TypeSuggestion(SInt64.instance),
                new TypeSuggestion(SChar.instance),
                new TypeSuggestion(SFloat.instance),
                new TypeSuggestion(SString.instance),
                new StaticConstantSuggestion(new StaticFieldConstantStaticVariable("intStorage", ApiRoot.class.getField("intStorage")))
        ));
    }

    private void assertSuggestions(String code, int line, int column, List<Suggestion> expected) {
        CompilationParameters parameters = new CompilationParametersBuilder()
                .setRoot(ApiRoot.class)
                .setInterface(Runnable.class)
                .build();
        BinderOutput output = new Binder(new Parser(new Lexer(new LexerInput(code)).lex()).parse(), parameters).bind();
        CompletionProvider<Suggestion> provider = new CompletionProvider<>(new TestSuggestionFactory());
        List<Suggestion> actual = provider.get(parameters, output, line, column);
        CompletionTestHelper.assertSuggestions(expected, actual);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
    }
}