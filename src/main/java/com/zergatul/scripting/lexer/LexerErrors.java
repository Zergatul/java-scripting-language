package com.zergatul.scripting.lexer;

import com.zergatul.scripting.ErrorCode;

public class LexerErrors {
    public static final ErrorCode UnexpectedSymbol = new ErrorCode("L001", "Unexpected symbol: \\u%s");
    public static final ErrorCode InvalidNumber = new ErrorCode("L002", "Invalid number '%s'");
    public static final ErrorCode NewlineInString = new ErrorCode("L003", "Newline in string");
}