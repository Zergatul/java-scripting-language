package com.zergatul.scripting.completion;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.documentation.DocumentationProvider;
import com.zergatul.scripting.formatting.MethodSignatureFormatter;
import com.zergatul.scripting.formatting.TypeDisplayFormatter;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;
import com.zergatul.scripting.utility.Lists;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class SuggestionInfoFactory implements SuggestionFactory<SuggestionInfo> {

    private final TypeDisplayFormatter typeFormatter;
    private final MethodSignatureFormatter methodSignatureFormatter;
    private final DocumentationProvider documentationProvider;

    public SuggestionInfoFactory() {
        this(new TypeDisplayFormatter(), new DocumentationProvider());
    }

    public SuggestionInfoFactory(TypeDisplayFormatter typeFormatter) {
        this(typeFormatter, new DocumentationProvider());
    }

    public SuggestionInfoFactory(TypeDisplayFormatter typeFormatter, DocumentationProvider documentationProvider) {
        this.typeFormatter = typeFormatter;
        this.methodSignatureFormatter = new MethodSignatureFormatter(typeFormatter);
        this.documentationProvider = documentationProvider;
    }

    @Override
    public SuggestionInfo getKeywordSuggestion(TokenType type) {
        String text;
        switch (type) {
            case META_CAST:
                text = "#cast";
                break;
            case META_TYPE:
                text = "#type";
                break;
            case META_TYPE_OF:
                text = "#typeof";
                break;
            default:
                text = type.toString().toLowerCase(Locale.ROOT);
                break;
        }
        return suggestion(text, SuggestionKind.KEYWORD);
    }

    @Override
    public List<SuggestionInfo> getTypeSuggestion(SType type) {
        if (type == SInt.instance) {
            return Lists.of(
                    typeSuggestion("int", type),
                    typeSuggestion("int32", type));
        }
        if (type == SInt64.instance) {
            return Lists.of(
                    typeSuggestion("long", type),
                    typeSuggestion("int64", type));
        }
        if (type == SFloat.instance) {
            return Lists.of(
                    typeSuggestion("float", type),
                    typeSuggestion("float64", type));
        }
        if (type.isPredefined()) {
            return Lists.of(typeSuggestion(type.toString(), type));
        }
        return Lists.of();
    }

    @Override
    public SuggestionInfo getCustomTypeSuggestion(Class<?> clazz) {
        CustomType type = clazz.getAnnotation(CustomType.class);
        return suggestion(type.name(), SuggestionKind.TYPE);
    }

    @Override
    public SuggestionInfo getClassSuggestion(ClassSymbol clazz) {
        return suggestion(clazz.getName(), SuggestionKind.TYPE);
    }

    @Override
    public SuggestionInfo getJavaTypeSuggestion(ClassSuggestion suggestion) {
        SuggestionKind kind;
        switch (suggestion.type()) {
            case PACKAGE:
                kind = SuggestionKind.PACKAGE;
                break;
            case CLASS:
                kind = SuggestionKind.TYPE;
                break;
            default:
                throw new InternalException();
        }
        return suggestion(suggestion.value(), kind);
    }

    @Override
    public SuggestionInfo getTypeAliasSuggestion(SAliasType type) {
        return suggestion(type.toString(), SuggestionKind.TYPE);
    }

    @Override
    public SuggestionInfo getThisSuggestion(SType type) {
        return suggestion("this", typeFormatter.format(type), null, SuggestionKind.KEYWORD);
    }

    @Override
    public SuggestionInfo getBaseSuggestion(SType type) {
        return suggestion("base", typeFormatter.format(type), null, SuggestionKind.KEYWORD);
    }

    @Override
    public SuggestionInfo getPropertySuggestion(PropertyReference property) {
        return suggestion(
                property.getName(),
                typeFormatter.format(property.getType()),
                documentationProvider.getPropertyDocumentation(property).orElse(null),
                SuggestionKind.PROPERTY);
    }

    @Override
    public SuggestionInfo getMethodSuggestion(MethodReference method) {
        if (method instanceof UnknownMethodReference) {
            throw new IllegalArgumentException("Cannot create suggestion for unknown method.");
        }

        return suggestion(
                method.getName(),
                methodSignatureFormatter.format(method),
                documentationProvider.getMethodDocumentation(method).orElse(null),
                SuggestionKind.METHOD);
    }

    @Override
    public SuggestionInfo getStaticConstantSuggestion(StaticFieldConstantStaticVariable variable) {
        return suggestion(variable.getName(), typeFormatter.format(variable.getType()), null, SuggestionKind.CONSTANT);
    }

    @Override
    public SuggestionInfo getStaticFieldSuggestion(DeclaredStaticVariable variable) {
        return suggestion(variable.getName(), typeFormatter.format(variable.getType()), null, SuggestionKind.VARIABLE);
    }

    @Override
    public SuggestionInfo getFunctionSuggestion(Function function) {
        StringBuilder sb = new StringBuilder();
        sb.append(typeFormatter.format(function.getFunctionType().getReturnType()));
        sb.append(' ');
        sb.append(function.getName());
        sb.append('(');
        for (int i = 0; i < function.getParameters().size(); i++) {
            MethodParameter parameter = function.getParameters().get(i);
            sb.append(typeFormatter.format(parameter.type()));
            sb.append(' ');
            sb.append(parameter.name());
            if (i < function.getParameters().size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(')');

        return suggestion(function.getName(), sb.toString(), null, SuggestionKind.FUNCTION);
    }

    @Override
    public SuggestionInfo getLocalVariableSuggestion(LocalVariable variable) {
        return suggestion(variable.getName(), typeFormatter.format(variable.getType()), null, SuggestionKind.VARIABLE);
    }

    @Override
    public SuggestionInfo getInputParameterSuggestion(String name, SType type) {
        return suggestion(name, typeFormatter.format(type), null, SuggestionKind.VARIABLE);
    }

    private SuggestionInfo typeSuggestion(String text, SType type) {
        return new SuggestionInfo(text, null, documentationProvider.getTypeDocs(type), text, SuggestionKind.TYPE);
    }

    private SuggestionInfo suggestion(String text, SuggestionKind kind) {
        return suggestion(text, null, null, kind);
    }

    private SuggestionInfo suggestion(String text, @Nullable String detail, @Nullable String documentation, SuggestionKind kind) {
        return new SuggestionInfo(text, detail, documentation, text, kind);
    }
}