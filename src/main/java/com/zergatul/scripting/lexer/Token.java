package com.zergatul.scripting.lexer;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;

public class Token implements Locatable {

    public final TokenType type;
    private final TextRange range;

    public Token(TokenType type, TextRange range) {
        this.type = type;
        this.range = range;
    }

    public TextRange getRange() {
        return this.range;
    }

    public boolean isMissing() {
        return this.range.isEmpty();
    }

    public String getRawValue(String code) {
        return range.extract(code);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Token other) {
            return other.type == type && other.range.equals(range);
        } else {
            return false;
        }
    }
}