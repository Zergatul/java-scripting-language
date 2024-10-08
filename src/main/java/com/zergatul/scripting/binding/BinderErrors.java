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
    public static final ErrorCode ArgumentCountMismatch = new ErrorCode("B014", "Function '%s' expects %d arguments");
    public static final ErrorCode CannotCastArguments = new ErrorCode("B015", "Invalid arguments");
    public static final ErrorCode InvalidFloatConstant = new ErrorCode("B016", "Cannot parse float constant");
    public static final ErrorCode NoLoop = new ErrorCode("B017", "No enclosing loop out of which to break or continue");
    public static final ErrorCode CannotApplyIncDec = new ErrorCode("B018", "Cannot apply '%s' to operand of type '%s'");
    public static final ErrorCode NewSupportArraysOnly = new ErrorCode("B019", "new expression support only arrays");
    public static final ErrorCode InvalidArrayCreation = new ErrorCode("B020", "Array creation must have array size or array initializer");
    public static final ErrorCode ExpressionCannotBeSet = new ErrorCode("B021", "The left-hand side of an assignment must be a variable, property or indexer");
    public static final ErrorCode CannotIterate = new ErrorCode("B022", "foreach statement cannot operate on expression of type '%s'");
    public static final ErrorCode ForEachTypesNotMatch = new ErrorCode("B023", "foreach variable type and expression type doesn't match");
    public static final ErrorCode LambdaIsInvalidInCurrentContext = new ErrorCode("B024", "Lambda expression is invalid in this context");
    public static final ErrorCode NotFunction = new ErrorCode("B025", "'%s' is not a function");
    public static final ErrorCode CannotCastArgument = new ErrorCode("B026", "Argument '%d' cannot convert from '%s' to '%s'");
    public static final ErrorCode EmptyReturnStatement = new ErrorCode("B027", "Cannot return void");
    public static final ErrorCode EmptyCharLiteral = new ErrorCode("B028", "Empty character literal");
    public static final ErrorCode TooManyCharsInCharLiteral = new ErrorCode("B029", "Too many characters in character literal");
    public static final ErrorCode AugmentedAssignmentInvalidType = new ErrorCode("B030", "'%s' operator on types '%s' and '%s' returns '%s', and it does not match with left side");
    public static final ErrorCode NotAllPathReturnValue = new ErrorCode("B031", "Not all code paths return a value");
    public static final ErrorCode CannotAwaitNonFuture = new ErrorCode("B032", "Only expressions with type Future<?> can be awaited");
    public static final ErrorCode InvalidCallee = new ErrorCode("B033", "Cannot call %s");
    public static final ErrorCode AwaitInNonAsyncContext = new ErrorCode("B034", "Cannot use 'await' in non-async context");
    public static final ErrorCode TypeNotDefined = new ErrorCode("B035", "Type '%s' is not defined");

    public static final ErrorCode InternalError = new ErrorCode("B999", "Internal error: %s");
}