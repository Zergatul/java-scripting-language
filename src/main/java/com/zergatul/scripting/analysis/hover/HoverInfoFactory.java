package com.zergatul.scripting.analysis.hover;

import com.zergatul.scripting.documentation.DocumentationProvider;
import com.zergatul.scripting.formatting.MethodSignatureFormatter;
import com.zergatul.scripting.formatting.TypeDisplayFormatter;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;
import com.zergatul.scripting.type.operation.BinaryOperation;

public class HoverInfoFactory implements HoverFactory<HoverInfo> {

    private final TypeDisplayFormatter typeFormatter;
    private final MethodSignatureFormatter methodSignatureFormatter;
    private final DocumentationProvider documentationProvider;

    public HoverInfoFactory() {
        this(new TypeDisplayFormatter(), new DocumentationProvider());
    }

    public HoverInfoFactory(TypeDisplayFormatter typeFormatter) {
        this(typeFormatter, new DocumentationProvider());
    }

    public HoverInfoFactory(TypeDisplayFormatter typeFormatter, DocumentationProvider documentationProvider) {
        this.typeFormatter = typeFormatter;
        this.methodSignatureFormatter = new MethodSignatureFormatter(typeFormatter);
        this.documentationProvider = documentationProvider;
    }

    @Override
    public HoverInfo getTypeHover(SType type) {
        String display = type == SInt64.instance ? "long" : typeFormatter.format(type);
        return new HoverInfo(typeFormatter.format(type), documentationProvider.getTypeDocs(type));
    }

    @Override
    public HoverInfo getTypeAliasHover(SAliasType type) {
        return new HoverInfo("typealias " + type + " = " + typeFormatter.format(type.getFinalType()));
    }

    @Override
    public HoverInfo getExternalParameterHover(ExternalParameter parameter) {
        return variable("(external parameter)", parameter);
    }

    @Override
    public HoverInfo getParameterHover(LocalParameter parameter) {
        return variable("(parameter)", parameter);
    }

    @Override
    public HoverInfo getRefParameterHover(LocalRefParameter parameter) {
        return new HoverInfo("(parameter) ref " + typeFormatter.format(parameter.getType()) + " " + parameter.getName());
    }

    @Override
    public HoverInfo getLocalVariableHover(LocalVariable variable) {
        return variable("(local variable)", variable);
    }

    @Override
    public HoverInfo getStaticConstantHover(StaticFieldConstantStaticVariable variable) {
        return variable("(external static constant)", variable);
    }

    @Override
    public HoverInfo getStaticVariableHover(StaticVariable variable) {
        return variable("(static variable)", variable);
    }

    @Override
    public HoverInfo getFunctionHover(Function function) {
        SStaticFunction type = function.getFunctionType();
        return new HoverInfo(
                typeFormatter.format(type.getReturnType()) + " " +
                function.getName() +
                methodSignatureFormatter.formatParameters(type.getParameters()));
    }

    @Override
    public HoverInfo getFunctionDeclarationHover(Function function) {
        HoverInfo hover = getFunctionHover(function);
        return new HoverInfo("(function) " + hover.signature(), hover.documentation());
    }

    @Override
    public HoverInfo getThisHover(SType type) {
        return new HoverInfo(typeFormatter.format(type) + " this");
    }

    @Override
    public HoverInfo getBaseHover(SType type) {
        return new HoverInfo(typeFormatter.format(type) + " base");
    }

    @Override
    public HoverInfo getMethodHover(MethodReference method) {
        String prefix = method instanceof ExtensionMethodReference ? "(extension) " : "";
        return new HoverInfo(
                prefix + methodSignatureFormatter.format(method),
                documentationProvider.getMethodDocumentation(method).orElse(null));
    }

    @Override
    public HoverInfo getPropertyHover(SType owner, PropertyReference property) {
        return new HoverInfo(
                "(property) " + typeFormatter.format(property.getType()) + " " + typeFormatter.format(owner) + "." + property.getName(),
                documentationProvider.getPropertyDocumentation(property).orElse(null));
    }

    @Override
    public HoverInfo getBinaryOperationHover(BinaryOperation operation) {
        return new HoverInfo(
                typeFormatter.format(operation.getResultType()) + " " +
                operation.getOperator() + "(" +
                typeFormatter.format(operation.getLeft()) + " left, " +
                typeFormatter.format(operation.getRight()) + " right)");
    }

    @Override
    public HoverInfo getConstructorHover(ConstructorReference constructor) {
        return new HoverInfo(
                "constructor " +
                typeFormatter.format(constructor.getOwner()) +
                methodSignatureFormatter.formatParameters(constructor.getParameters()),
                documentationProvider.getConstructorDocumentation(constructor).orElse(null));
    }

    private HoverInfo variable(String description, Variable variable) {
        return new HoverInfo(description + " " + typeFormatter.format(variable.getType()) + " " + variable.getName());
    }
}