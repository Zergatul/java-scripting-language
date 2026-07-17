package com.zergatul.scripting.tests.hover;

import com.zergatul.scripting.analysis.Analyzer;
import com.zergatul.scripting.analysis.hover.HoverProvider;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.tests.utility.CursorHelper;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class HoverTestHelper {

    public static void assertHover(String code, Class<?> root, int length, List<String> expected) {
        assertHover(code, buildCompilationParameters(root), length, expected);
    }

    public static void assertHover(String code, Class<?> root, Class<?> customType, int length, List<String> expected) {
        assertHover(code, buildCompilationParameters(root, customType), length, expected);
    }

    private static void assertHover(String code, CompilationParameters parameters, int length, List<String> expected) {
        CursorHelper.Result result = CursorHelper.parse(code);

        BinderOutput output = new Analyzer().analyze(result.code(), parameters).binderOutput();
        HoverProvider.HoverResponse actual = new HoverProvider().get(output, result.line(), result.column());

        Assertions.assertIterableEquals(expected, actual.content());
        Assertions.assertEquals(result.range().extend(length), actual.range());
    }

    private static CompilationParameters buildCompilationParameters(Class<?> root) {
        return new CompilationParametersBuilder()
                .setRoot(root)
                .setInterface(Runnable.class)
                .build();
    }

    private static CompilationParameters buildCompilationParameters(Class<?> root, Class<?> customType) {
        return new CompilationParametersBuilder()
                .setRoot(root)
                .setInterface(Runnable.class)
                .addCustomType(customType)
                .build();
    }
}