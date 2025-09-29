package com.zergatul.scripting.tests.utility;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.Trivia;
import com.zergatul.scripting.parser.ParserOutput;
import org.junit.jupiter.api.Assertions;

public class TreeValidation {

    public static void check(LexerOutput lexerOutput, ParserOutput parserOutput, String code) {
        checkLexer(lexerOutput, code);
        //Assertions.assertEquals(code, parserOutput.unit().asFullSource());
    }

    public static void checkLexer(LexerOutput lexerOutput, String code) {
        lexerOutput.tokens().rollback(0);

        Cursor cursor = new Cursor(code);
        for (Token token : lexerOutput.tokens()) {
            for (Trivia trivia : token.getLeadingTrivia()) {
                cursor.verifyAndMove(trivia);
            }
            cursor.verifyAndMove(token);
            for (Trivia trivia : token.getTrailingTrivia()) {
                cursor.verifyAndMove(trivia);
            }
        }

        Assertions.assertEquals(code.length(), cursor.position);
        Assertions.assertEquals(code, cursor.builder.toString());
    }

    private static class Cursor {

        public final String code;
        public final StringBuilder builder;
        public int line;
        public int column;
        public int position;

        public Cursor(String code) {
            this.code = code;
            this.builder = new StringBuilder();
            this.line = 1;
            this.column = 1;
            this.position = 0;
        }

        public void verifyAndMove(Token token) {
            TextRange range = token.getRange();
            Assertions.assertEquals(this.line, range.getLine1());
            Assertions.assertEquals(this.column, range.getColumn1());
            Assertions.assertEquals(this.position, range.getPosition());

            builder.append(token.asSource(code));

            this.line = range.getLine2();
            this.column = range.getColumn2();
            this.position += range.getLength();
        }
    }
}