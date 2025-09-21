package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.tests.framework.ComparatorTest;

public abstract class ParserTestBase extends ComparatorTest {
    protected ParserOutput parse(String code) {
        return new Parser(new Lexer(new LexerInput(code)).lex()).parse();
    }
}