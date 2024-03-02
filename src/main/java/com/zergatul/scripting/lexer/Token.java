package com.zergatul.scripting.lexer;

public class Token {

    public final TokenType type;
    public final int line;
    public final int column;
    public final int position;
    public final int length;

    public Token(TokenType type, int line, int column, int position, int length) {
        this.type = type;
        this.line = line;
        this.column = column;
        this.position = position;
        this.length = length;
    }

    public String getRawValue(String code) {
        return code.substring(position, position + length);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Token other) {
            return  other.type == type &&
                    other.line == line &&
                    other.column == column &&
                    other.position == position &&
                    other.length == length;
        } else {
            return false;
        }
    }
}