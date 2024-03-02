package com.zergatul.scripting.lexer;

import com.zergatul.scripting.ErrorCode;

public class LexerErrors {
    public static final ErrorCode UnexpectedSymbol = new ErrorCode("L001", "Unexpected symbol: \\u%s.");
}