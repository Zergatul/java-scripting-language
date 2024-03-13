package com.zergatul.scripting.binding;

import com.zergatul.scripting.ErrorCode;

public class BinderErrors {
    public static final ErrorCode CannotImplicitlyConvert = new ErrorCode("B001", "Cannot implicitly convert type '%s' to '%s'");
    public static final ErrorCode UnaryOperatorNotDefined = new ErrorCode("B002", "Operator '%s' cannot be applied to operand of type '%s'");
    public static final ErrorCode BinaryOperatorNotDefined = new ErrorCode("B003", "Operator '%s' cannot be applied to operands of type '%s' and '%s'");
    public static final ErrorCode IntegerConstantTooSmall = new ErrorCode("B004", "Integer constant is too small");
    public static final ErrorCode IntegerConstantTooLarge = new ErrorCode("B005", "Integer constant is too large");
    public static final ErrorCode CannotDetermineConditionalExpressionType = new ErrorCode("B006", "Type of conditional expression cannot be determined because there is no implicit conversion between '%s' and '%s'");
    public static final ErrorCode CannotApplyIndex = new ErrorCode("B007", "Cannot apply indexing with [] to an expression of type '%s'");
    public static final ErrorCode SymbolAlreadyDeclared = new ErrorCode("B008", "A symbol named '%s' is already defined in this or one of the parent scopes");
    public static final ErrorCode NameDoesNotExist = new ErrorCode("B009", "The name '%s' does not exist in the current context");
    public static final ErrorCode MemberDoesNotExist = new ErrorCode("B010", "'%s' does not contain a definition for '%s'");
    public static final ErrorCode FunctionExpected = new ErrorCode("B011", "Function expected");
    public static final ErrorCode NonInvocableMember = new ErrorCode("B012", "Non-invocable member '%s.%s' cannot be used like a method");
    public static final ErrorCode NoOverloadedMethods = new ErrorCode("B013", "No overload for method '%s' takes %d arguments");
    public static final ErrorCode ArgumentCountMismatch = new ErrorCode("B014", "Function does not take %d arguments");
    public static final ErrorCode CannotCastArguments = new ErrorCode("B015", "Invalid arguments");
    public static final ErrorCode InternalError = new ErrorCode("B999", "Internal error: %s");
}