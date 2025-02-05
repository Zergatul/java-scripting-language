package com.zergatul.scripting.completion;

import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.PropertyReference;
import com.zergatul.scripting.type.SType;

import java.util.List;

public interface SuggestionFactory<T> {
    T getStaticKeywordSuggestion();
    T getVoidKeywordSuggestion();
    T getAwaitKeywordSuggestion();
    T getLetKeywordSuggestion();
    List<T> getTypeSuggestion(SType type);
    T getCustomTypeSuggestion(Class<?> clazz);
    T getPropertySuggestion(PropertyReference property);
    T getMethodSuggestion(MethodReference method);
    List<T> getCommonStatementStartSuggestions();
    T getStaticConstantSuggestion(StaticFieldConstantStaticVariable variable);
    T getStaticFieldSuggestion(DeclaredStaticVariable variable);
    T getFunctionSuggestion(Function function);
    T getLocalVariableSuggestion(LocalVariable variable);
    T getInputParameterSuggestion(String name, SType type);
}