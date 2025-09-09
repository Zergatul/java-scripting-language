package com.zergatul.scripting.tests.lexer;

import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.tests.utility.TreeValidation;
import com.zergatul.scripting.tests.framework.ComparatorTest;

public abstract class LexerTestBase extends ComparatorTest {
    protected LexerOutput lex(String code) {
        LexerOutput lexerOutput = new Lexer(new LexerInput(code)).lex();
        TreeValidation.checkLexer(lexerOutput, code);
        return lexerOutput;
    }
}