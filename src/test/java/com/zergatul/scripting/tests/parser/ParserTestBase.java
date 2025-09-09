package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.tests.utility.TreeValidation;
import com.zergatul.scripting.tests.framework.ComparatorTest;

public abstract class ParserTestBase extends ComparatorTest {
    protected ParserOutput parse(String code) {
        LexerOutput lexerOutput = new Lexer(new LexerInput(code)).lex();
        ParserOutput parserOutput = new Parser(lexerOutput).parse();
        TreeValidation.check(lexerOutput, parserOutput, code);
        return parserOutput;
    }
}