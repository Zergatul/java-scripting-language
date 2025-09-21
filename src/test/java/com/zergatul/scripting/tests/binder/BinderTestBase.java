package com.zergatul.scripting.tests.binder;

import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.tests.framework.ComparatorTest;

public class BinderTestBase extends ComparatorTest {
    protected BinderOutput bind(Class<?> root, String code) {
        CompilationParameters parameters = new CompilationParametersBuilder()
                .setRoot(root)
                .build();
        return new Binder(new Parser(new Lexer(new LexerInput(code)).lex()).parse(), parameters).bind();
    }
}