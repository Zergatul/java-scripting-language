package com.zergatul.scripting.parser;

import com.zergatul.scripting.ErrorCode;

public class ParserErrors {
    public static final ErrorCode OpenCurlyBracketExpected = new ErrorCode("C001", "{ expected, found %s");
    public static final ErrorCode CloseCurlyBracketExpected = new ErrorCode("C002", "} expected, found %s");
    public static final ErrorCode CloseSquareBracketExpected = new ErrorCode("C003", "] expected, found %s");
    public static final ErrorCode IdentifierExpected = new ErrorCode("C004", "Identifier expected, found %s");
    public static final ErrorCode SemicolonOrEqualExpected = new ErrorCode("C005", "; or = expected, found %s");
    public static final ErrorCode ExpressionExpected = new ErrorCode("C006", "Expression expected, found %s");
    public static final ErrorCode ExpressionOrCloseParenthesesExpected = new ErrorCode("C007", "Expression or ) expected, found %s");
    public static final ErrorCode CommaOrCloseParenthesesExpected = new ErrorCode("C008", ", or ) expected, found %s");
    public static final ErrorCode StatementExpected = new ErrorCode("C009", "Statement expected, found %s");
    public static final ErrorCode SemicolonExpected = new ErrorCode("C010", "; expected");
    public static final ErrorCode SimpleStatementExpected = new ErrorCode("C011", "Simple statement expected, found %s");
    public static final ErrorCode CannotApplyIncDec = new ErrorCode("C012", "The operand of an increment or decrement must be a variable, property or indexer");
    public static final ErrorCode ForEachTypeIdentifierRequired = new ErrorCode("C013", "Type and identifier are both required in a foreach statement");
    public static final ErrorCode InExpected = new ErrorCode("C014", "in expected");
    public static final ErrorCode InvalidRefExpression = new ErrorCode("C015", "A ref value should be assignable variable");
    public static final ErrorCode LeftParenthesisExpected = new ErrorCode("C016", "( expected, found %s");
    public static final ErrorCode RightParenthesisExpected = new ErrorCode("C017", ") expected, found %s");
    public static final ErrorCode ColonExpected = new ErrorCode("C018", ": expected");
    public static final ErrorCode CannotUseLet = new ErrorCode("C019", "Cannot use 'let' without initialization");
    public static final ErrorCode CommaOrCloseCurlyBracketExpected = new ErrorCode("C020", "'}' or ',' expected");
    public static final ErrorCode CommaOrCloseSquareBracketExpected = new ErrorCode("C021", "']' or ',' expected");
    public static final ErrorCode TypeExpected = new ErrorCode("C022", "Type expected, found '%s'");
    public static final ErrorCode OpenTriangleBracketExpected = new ErrorCode("C023", "< expected, found %s");
    public static final ErrorCode CloseTriangleBracketExpected = new ErrorCode("C024", "> expected, found %s");
    public static final ErrorCode InvalidNewExpression = new ErrorCode("C025", "A new expression requires '[' or '('");
    public static final ErrorCode SemicolonOrParenthesisExpected = new ErrorCode("C026", "; or ( expected, found %s");
    public static final ErrorCode FieldInitializersNotSupported = new ErrorCode("C027", "Field initializers not supported");
    public static final ErrorCode CurlyBracketOrArrowExpected = new ErrorCode("C028", "{ or => expected, found %s");
    public static final ErrorCode ArrowExpected = new ErrorCode("C029", "=> expected");
    public static final ErrorCode NotAStatement = new ErrorCode("C030", "Not a statement");
    public static final ErrorCode IdentifierOrCloseParenthesesExpected = new ErrorCode("C031", "Identifier or ) expected, found %s");
    public static final ErrorCode TypeOrCloseParenthesesExpected = new ErrorCode("C032", "Type or ) expected, found '%s'");
    public static final ErrorCode ExpressionOrCloseSquaredBracketExpected = new ErrorCode("C033", "Expression or ] expected, found %s");
    public static final ErrorCode ExpressionOrCloseBraceExpected = new ErrorCode("C034", "Expression or } expected, found %s");
    public static final ErrorCode CommaOrCloseBraceExpected = new ErrorCode("C035", "'}' or ',' expected");
}