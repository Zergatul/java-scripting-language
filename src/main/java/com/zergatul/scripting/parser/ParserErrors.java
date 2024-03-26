package com.zergatul.scripting.parser;

import com.zergatul.scripting.ErrorCode;

public class ParserErrors {
    public static final ErrorCode OpenCurlyBracketExpected = new ErrorCode("C001", "{ expected, found %s");
    public static final ErrorCode CloseSquareBracketExpected = new ErrorCode("C002", "] expected, found %s");
    public static final ErrorCode IdentifierExpected = new ErrorCode("C003", "Identifier expected, found %s");
    public static final ErrorCode SemicolonOrEqualExpected = new ErrorCode("C004", "; or = expected, found %s");
    public static final ErrorCode ExpressionExpected = new ErrorCode("C005", "Expression expected, found %s");
    public static final ErrorCode ExpressionOrCloseParenthesesExpected = new ErrorCode("C006", "Expression or ) expected, found %s");
    public static final ErrorCode CommaOrCloseParenthesesExpected = new ErrorCode("C006", ", or ) expected, found %s");
    public static final ErrorCode StatementExpected = new ErrorCode("C007", "Statement expected, found %s");
    public static final ErrorCode SemicolonExpected = new ErrorCode("C008", "; expected");
    public static final ErrorCode SimpleStatementExpected = new ErrorCode("C009", "Simple statement expected, found %s");
    public static final ErrorCode CannotApplyIncDec = new ErrorCode("C010", "The operand of an increment or decrement must be a variable, property or indexer");
    public static final ErrorCode ForEachTypeIdentifierRequired = new ErrorCode("C011", "Type and identifier are both required in a foreach statement");
    public static final ErrorCode InExpected = new ErrorCode("C012", "in expected");
}