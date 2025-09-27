package com.zergatul.scripting.tests.parser;

import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.tests.framework.ComparatorTest;
import org.junit.jupiter.api.Assertions;

public abstract class ParserTestBase extends ComparatorTest {
    protected ParserOutput parse(String code) {
        ParserOutput output = new Parser(new Lexer(new LexerInput(code)).lex()).parse();
        Assertions.assertEquals(code, output.unit().asFullSource());
        return output;
    }
}