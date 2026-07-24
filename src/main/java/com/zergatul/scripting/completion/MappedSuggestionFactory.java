package com.zergatul.scripting.completion;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;

import java.util.List;

public class MappedSuggestionFactory<T> implements SuggestionFactory<T> {

    private final SuggestionFactory<SuggestionInfo> factory;
    private final SuggestionMapper<T> mapper;

    public MappedSuggestionFactory(SuggestionMapper<T> mapper) {
        this(new SuggestionInfoFactory(), mapper);
    }

    public MappedSuggestionFactory(SuggestionFactory<SuggestionInfo> factory, SuggestionMapper<T> mapper) {
        this.factory = factory;
        this.mapper = mapper;
    }

    @Override
    public T getKeywordSuggestion(TokenType type) {
        return map(factory.getKeywordSuggestion(type));
    }

    @Override
    public List<T> getTypeSuggestion(SType type) {
        return factory.getTypeSuggestion(type).stream().map(mapper::map).toList();
    }

    @Override
    public T getCustomTypeSuggestion(Class<?> clazz) {
        return map(factory.getCustomTypeSuggestion(clazz));
    }

    @Override
    public T getClassSuggestion(ClassSymbol clazz) {
        return map(factory.getClassSuggestion(clazz));
    }

    @Override
    public T getJavaTypeSuggestion(ClassSuggestion suggestion) {
        return map(factory.getJavaTypeSuggestion(suggestion));
    }

    @Override
    public T getTypeAliasSuggestion(SAliasType type) {
        return map(factory.getTypeAliasSuggestion(type));
    }

    @Override
    public T getThisSuggestion(SType type) {
        return map(factory.getThisSuggestion(type));
    }

    @Override
    public T getBaseSuggestion(SType type) {
        return map(factory.getBaseSuggestion(type));
    }

    @Override
    public T getPropertySuggestion(PropertyReference property) {
        return map(factory.getPropertySuggestion(property));
    }

    @Override
    public T getMethodSuggestion(MethodReference method) {
        return map(factory.getMethodSuggestion(method));
    }

    @Override
    public T getStaticConstantSuggestion(StaticFieldConstantStaticVariable variable) {
        return map(factory.getStaticConstantSuggestion(variable));
    }

    @Override
    public T getStaticFieldSuggestion(DeclaredStaticVariable variable) {
        return map(factory.getStaticFieldSuggestion(variable));
    }

    @Override
    public T getFunctionSuggestion(Function function) {
        return map(factory.getFunctionSuggestion(function));
    }

    @Override
    public T getLocalVariableSuggestion(LocalVariable variable) {
        return map(factory.getLocalVariableSuggestion(variable));
    }

    @Override
    public T getInputParameterSuggestion(String name, SType type) {
        return map(factory.getInputParameterSuggestion(name, type));
    }

    private T map(SuggestionInfo suggestion) {
        return mapper.map(suggestion);
    }
}