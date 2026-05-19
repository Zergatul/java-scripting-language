package com.zergatul.scripting.type;

public enum ConversionType {
    IDENTITY,
    IMPLICIT_CAST,
    EMPTY_ARRAY,
    FUNCTION_TO_INTERFACE,
    FUNCTION_TO_GENERIC,
    LAMBDA_BINDING,
    LAMBDA_BINDING_TO_CLASS,
    METHOD_GROUP_TO_INTERFACE,
    METHOD_GROUP_TO_GENERIC
}