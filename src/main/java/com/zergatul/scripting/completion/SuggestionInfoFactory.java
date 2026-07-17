package com.zergatul.scripting.completion;

import com.zergatul.scripting.formatting.MethodSignatureFormatter;
import com.zergatul.scripting.formatting.TypeDisplayFormatter;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class SuggestionInfoFactory implements SuggestionFactory<SuggestionInfo> {

    private final TypeDisplayFormatter typeFormatter;
    private final MethodSignatureFormatter methodSignatureFormatter;

    public SuggestionInfoFactory() {
        this(new TypeDisplayFormatter());
    }

    public SuggestionInfoFactory(TypeDisplayFormatter typeFormatter) {
        this.typeFormatter = typeFormatter;
        this.methodSignatureFormatter = new MethodSignatureFormatter(typeFormatter);
    }

    @Override
    public SuggestionInfo getKeywordSuggestion(TokenType type) {
        String text = switch (type) {
            case META_CAST -> "#cast";
            case META_TYPE -> "#type";
            case META_TYPE_OF -> "#typeof";
            default -> type.toString().toLowerCase(Locale.ROOT);
        };
        return suggestion(text, SuggestionKind.KEYWORD);
    }

    @Override
    public List<SuggestionInfo> getTypeSuggestion(SType type) {
        if (type == SInt.instance) {
            return List.of(
                    typeSuggestion("int", type),
                    typeSuggestion("int32", type));
        }
        if (type == SInt64.instance) {
            return List.of(
                    typeSuggestion("long", type),
                    typeSuggestion("int64", type));
        }
        if (type == SFloat.instance) {
            return List.of(
                    typeSuggestion("float", type),
                    typeSuggestion("float64", type));
        }
        if (type.isPredefined()) {
            return List.of(typeSuggestion(type.toString(), type));
        }
        return List.of();
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
                property.getDescription().orElse(null),
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
                method.getDescription().orElse(null),
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
        return suggestion(function.getName(), function.getFunctionType().toString(), null, SuggestionKind.FUNCTION);
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
        return new SuggestionInfo(text, null, getTypeDocumentation(type), text, SuggestionKind.TYPE);
    }

    private SuggestionInfo suggestion(String text, SuggestionKind kind) {
        return suggestion(text, null, null, kind);
    }

    private SuggestionInfo suggestion(String text, @Nullable String detail, @Nullable String documentation, SuggestionKind kind) {
        return new SuggestionInfo(text, detail, documentation, text, kind);
    }

    private @Nullable String getTypeDocumentation(SType type) {
        if (type == SBoolean.instance) {
            return "true or false value";
        }
        if (type == SInt8.instance) {
            return "8-bit signed integer";
        }
        if (type == SInt16.instance) {
            return "16-bit signed integer";
        }
        if (type == SInt.instance) {
            return "32-bit signed integer";
        }
        if (type == SInt64.instance) {
            return "64-bit signed integer";
        }
        if (type == SChar.instance) {
            return "Single character";
        }
        if (type == SFloat32.instance) {
            return "Single-precision floating-point number";
        }
        if (type == SFloat.instance) {
            return "Double-precision floating-point number";
        }
        if (type == SString.instance) {
            return "Text as sequence of characters";
        }
        return null;
    }
}