package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.completion.SuggestionFactory;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.tests.completion.suggestions.*;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.PropertyReference;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class TestSuggestionFactory implements SuggestionFactory<Suggestion> {

    @Override
    public Suggestion getStaticKeywordSuggestion() {
        return new StaticKeywordSuggestion();
    }

    @Override
    public Suggestion getVoidKeywordSuggestion() {
        return new VoidKeywordSuggestion();
    }

    @Override
    public Suggestion getAwaitKeywordSuggestion() {
        return new AwaitKeywordSuggestion();
    }

    @Override
    public Suggestion getLetKeywordSuggestion() {
        return new LetKeywordSuggestion();
    }

    @Override
    public List<Suggestion> getTypeSuggestion(SType type) {
        return List.of(new TypeSuggestion(type));
    }

    @Override
    public Suggestion getCustomTypeSuggestion(Class<?> clazz) {
        return new CustomTypeSuggestion(clazz);
    }

    @Override
    public Suggestion getPropertySuggestion(PropertyReference property) {
        return new PropertySuggestion(property);
    }

    @Override
    public Suggestion getMethodSuggestion(MethodReference method) {
        return new MethodSuggestion(method);
    }

    @Override
    public List<Suggestion> getCommonStatementStartSuggestions() {
        return List.of();
    }

    @Override
    public Suggestion getStaticConstantSuggestion(StaticFieldConstantStaticVariable variable) {
        return new StaticConstantSuggestion(variable);
    }

    @Override
    public Suggestion getStaticFieldSuggestion(DeclaredStaticVariable variable) {
        return new StaticFieldSuggestion(variable);
    }

    @Override
    public Suggestion getFunctionSuggestion(Function function) {
        return new FunctionSuggestion(function);
    }

    @Override
    public Suggestion getLocalVariableSuggestion(LocalVariable variable) {
        return new LocalVariableSuggestion(variable);
    }

    @Override
    public Suggestion getInputParameterSuggestion(String name, SType type) {
        return new InputParameterSuggestion(name, type);
    }
}