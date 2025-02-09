package com.zergatul.scripting.tests.completion.helpers;

import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.completion.CompletionProvider;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.tests.completion.suggestions.Suggestion;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CompletionTestHelper {

    private static final String CURSOR = "<cursor>";

    public static void assertSuggestions(Class<?> root, String code, Function<TestCompletionContext, List<Suggestion>> expectedFactory) {
        if (!code.contains(CURSOR)) {
            Assertions.fail();
            return;
        }

        int line = -1, column = -1;
        String[] lines = code.lines().toArray(String[]::new);
        for (int i = 0; i < lines.length; i++) {
            int index = lines[i].indexOf(CURSOR);
            if (index >= 0) {
                line = i + 1;
                column = index + 1;
                break;
            }
        }
        if (line == -1) {
            Assertions.fail();
            return;
        }

        code = code.replace(CURSOR, "");

        CompilationParameters parameters = new CompilationParametersBuilder()
                .setRoot(root)
                .setInterface(Runnable.class)
                .build();
        BinderOutput output = new Binder(new Parser(new Lexer(new LexerInput(code)).lex()).parse(), parameters).bind();
        CompletionProvider<Suggestion> provider = new CompletionProvider<>(new TestSuggestionFactory());
        List<Suggestion> actual = provider.get(parameters, output, line, column);
        CompletionTestHelper.assertSuggestions(expectedFactory.apply(new TestCompletionContext(parameters, output)), actual);
    }

    private static void assertSuggestions(List<Suggestion> expected, List<Suggestion> actual) {
        Assertions.assertEquals(expected.size(), actual.size());

        expected = new ArrayList<>(expected);
        actual = new ArrayList<>(actual);
        while (!expected.isEmpty()) {
            int index = -1;
            for (int i = 0; i < expected.size(); i++) {
                if (expected.get(0).equals(actual.get(i))) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                Assertions.fail();
            }
            expected.remove(0);
            actual.remove(index);
        }
    }
}