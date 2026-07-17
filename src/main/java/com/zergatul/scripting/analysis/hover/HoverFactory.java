package com.zergatul.scripting.analysis.hover;

import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;
import com.zergatul.scripting.type.operation.BinaryOperation;

public interface HoverFactory<T> {
    T getTypeHover(SType type);
    T getTypeAliasHover(SAliasType type);
    T getExternalParameterHover(ExternalParameter parameter);
    T getParameterHover(LocalParameter parameter);
    T getRefParameterHover(LocalRefParameter parameter);
    T getLocalVariableHover(LocalVariable variable);
    T getStaticConstantHover(StaticFieldConstantStaticVariable variable);
    T getStaticVariableHover(StaticVariable variable);
    T getFunctionHover(Function function);
    T getFunctionDeclarationHover(Function function);
    T getThisHover(SType type);
    T getBaseHover(SType type);
    T getMethodHover(MethodReference method);
    T getPropertyHover(SType owner, PropertyReference property);
    T getBinaryOperationHover(BinaryOperation operation);
    T getConstructorHover(ConstructorReference constructor);
}