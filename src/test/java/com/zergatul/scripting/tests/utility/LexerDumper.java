package com.zergatul.scripting.tests.utility;

import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.lexer.Token;

public class LexerDumper extends Dumper {

    public String dump(LexerOutput output) {
        output.tokens().rollback(0);
        reset();

        for (Token token : output.tokens()) {
            dump(token);
            commaBreak();
        }

        if (!sb.isEmpty()) {
            sb.delete(sb.length() - 2, sb.length());
        }

        return sb.toString();
    }
}