package com.zergatul.scripting.lexer;

public enum TokenType {
    WHITESPACE,
    LEFT_PARENTHESES,
    RIGHT_PARENTHESES,
    LEFT_SQUARE_BRACKET,
    RIGHT_SQUARE_BRACKET,
    LEFT_CURLY_BRACKET,
    RIGHT_CURLY_BRACKET,
    DOT,
    COMMA,
    COLON,
    SEMICOLON,
    EXCLAMATION,
    AMPERSAND,
    PIPE,
    PLUS,
    PLUS_PLUS,
    MINUS,
    MINUS_MINUS,
    ASTERISK,
    SLASH,
    PERCENT,
    LESS,
    GREATER,
    IDENTIFIER,
    FALSE,
    TRUE,
    INTEGER_LITERAL,
    FLOAT_LITERAL,
    STRING_LITERAL,
    CHAR_LITERAL,
    INVALID_NUMBER,
    END_OF_FILE,
    EQUAL,
    QUESTION,
    BOOLEAN,
    INT,
    FLOAT,
    STRING,
    CHAR,
    NEW,
    EQUAL_EQUAL,
    EQUAL_GREATER,
    EXCLAMATION_EQUAL,
    AMPERSAND_AMPERSAND,
    AMPERSAND_EQUAL,
    PIPE_PIPE,
    PIPE_EQUAL,
    LESS_EQUAL,
    GREATER_EQUAL,
    PLUS_EQUAL,
    MINUS_EQUAL,
    ASTERISK_EQUAL,
    SLASH_EQUAL,
    PERCENT_EQUAL,
    IF,
    ELSE,
    RETURN,
    INVALID,
    FOR,
    FOREACH,
    WHILE,
    BREAK,
    CONTINUE,
    IN,
    STATIC,
    VOID,
    REF
}