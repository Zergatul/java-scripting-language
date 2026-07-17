package com.zergatul.scripting.analysis.hover;

import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;
import com.zergatul.scripting.type.operation.BinaryOperation;

public class MappedHoverFactory<T> implements HoverFactory<T> {

    private final HoverFactory<HoverInfo> factory;
    private final HoverMapper<T> mapper;

    public MappedHoverFactory(HoverMapper<T> mapper) {
        this(new HoverInfoFactory(), mapper);
    }

    public MappedHoverFactory(HoverFactory<HoverInfo> factory, HoverMapper<T> mapper) {
        this.factory = factory;
        this.mapper = mapper;
    }

    @Override
    public T getTypeHover(SType type) {
        return map(factory.getTypeHover(type));
    }

    @Override
    public T getTypeAliasHover(SAliasType type) {
        return map(factory.getTypeAliasHover(type));
    }

    @Override
    public T getExternalParameterHover(ExternalParameter parameter) {
        return map(factory.getExternalParameterHover(parameter));
    }

    @Override
    public T getParameterHover(LocalParameter parameter) {
        return map(factory.getParameterHover(parameter));
    }

    @Override
    public T getRefParameterHover(LocalRefParameter parameter) {
        return map(factory.getRefParameterHover(parameter));
    }

    @Override
    public T getLocalVariableHover(LocalVariable variable) {
        return map(factory.getLocalVariableHover(variable));
    }

    @Override
    public T getStaticConstantHover(StaticFieldConstantStaticVariable variable) {
        return map(factory.getStaticConstantHover(variable));
    }

    @Override
    public T getStaticVariableHover(StaticVariable variable) {
        return map(factory.getStaticVariableHover(variable));
    }

    @Override
    public T getFunctionHover(Function function) {
        return map(factory.getFunctionHover(function));
    }

    @Override
    public T getFunctionDeclarationHover(Function function) {
        return map(factory.getFunctionDeclarationHover(function));
    }

    @Override
    public T getThisHover(SType type) {
        return map(factory.getThisHover(type));
    }

    @Override
    public T getBaseHover(SType type) {
        return map(factory.getBaseHover(type));
    }

    @Override
    public T getMethodHover(MethodReference method) {
        return map(factory.getMethodHover(method));
    }

    @Override
    public T getPropertyHover(SType owner, PropertyReference property) {
        return map(factory.getPropertyHover(owner, property));
    }

    @Override
    public T getBinaryOperationHover(BinaryOperation operation) {
        return map(factory.getBinaryOperationHover(operation));
    }

    @Override
    public T getConstructorHover(ConstructorReference constructor) {
        return map(factory.getConstructorHover(constructor));
    }

    private T map(HoverInfo hover) {
        return mapper.map(hover);
    }
}