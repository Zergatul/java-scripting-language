package com.zergatul.scripting.lexer;

import com.zergatul.scripting.ErrorCode;

public class LexerErrors {
    public static final ErrorCode UnexpectedSymbol = new ErrorCode("L001", "Unexpected symbol: \\u%s");
    public static final ErrorCode InvalidNumber = new ErrorCode("L002", "Invalid number '%s'");
    public static final ErrorCode NewlineInString = new ErrorCode("L003", "Newline in string");
    public static final ErrorCode NewlineInCharacter = new ErrorCode("L004", "Newline in character");
    public static final ErrorCode UnfinishedString = new ErrorCode("L005", "Unfinished string");
    public static final ErrorCode UnknownMetaFunction = new ErrorCode("L006", "Expected a known meta function after '#', but got '%s'");
}