package com.zergatul.scripting.tests.lexer;

import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;

public abstract class LexerTestBase {
    protected LexerOutput lex(String code) {
        return new Lexer(new LexerInput(code)).lex();
    }
}