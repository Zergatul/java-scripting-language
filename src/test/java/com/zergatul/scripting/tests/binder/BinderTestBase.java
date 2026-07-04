package com.zergatul.scripting.tests.binder;

import com.zergatul.scripting.analysis.Analyzer;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.tests.framework.ComparatorTest;

public class BinderTestBase extends ComparatorTest {
    protected BinderOutput bind(Class<?> root, String code) {
        CompilationParameters parameters = new CompilationParametersBuilder()
                .setRoot(root)
                .build();
        return new Analyzer().analyze(code, parameters).binderOutput();
    }
}