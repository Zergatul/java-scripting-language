package com.zergatul.scripting.lexer;

import com.zergatul.scripting.SingleLineTextRange;

public class EndOfFileToken extends Token {

    public static final EndOfFileToken instance = new EndOfFileToken();

    private EndOfFileToken() {
        super(TokenType.END_OF_FILE, new SingleLineTextRange(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    @Override
    public String getRawValue(String code) {
        return "<EOF>";
    }
}