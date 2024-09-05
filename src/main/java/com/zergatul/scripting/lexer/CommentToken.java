package com.zergatul.scripting.lexer;

import com.zergatul.scripting.TextRange;

public class CommentToken extends Token {

    // if comment goes till the end of the line
    public final boolean ending;

    public CommentToken(boolean ending, TextRange range) {
        super(TokenType.COMMENT, range);
        this.ending = ending;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CommentToken other) {
            return other.ending == ending && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}