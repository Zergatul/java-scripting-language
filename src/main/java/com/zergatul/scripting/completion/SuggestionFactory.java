package com.zergatul.scripting.completion;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.PropertyReference;
import com.zergatul.scripting.type.SDeclaredType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public interface SuggestionFactory<T> {
    T getKeywordSuggestion(TokenType type);
    List<T> getTypeSuggestion(SType type);
    T getCustomTypeSuggestion(Class<?> clazz);
    T getClassSuggestion(ClassSymbol clazz);
    T getThisSuggestion(SType type);
    T getBaseSuggestion(SType type);
    T getPropertySuggestion(PropertyReference property);
    T getMethodSuggestion(MethodReference method);
    T getStaticConstantSuggestion(StaticFieldConstantStaticVariable variable);
    T getStaticFieldSuggestion(DeclaredStaticVariable variable);
    T getFunctionSuggestion(Function function);
    T getLocalVariableSuggestion(LocalVariable variable);
    T getInputParameterSuggestion(String name, SType type);
}