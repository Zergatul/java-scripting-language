package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;

public class SSyntheticInterface extends SSyntheticType {

    private final MethodDefinition definition;

    public SSyntheticInterface(MethodDefinition definition) {
        this.definition = definition;
    }

    public MethodDefinition getDefinition() {
        return definition;
    }

    @Override
    public boolean isCompatibleWith(SType other) {
        return MemberLookup.getMethods(other).stream()
                .filter(m -> !m.isStatic())
                .anyMatch(definition::matches);
    }

    public MethodReference extractMethod(SType other) {
        return MemberLookup.getMethods(other).stream()
                .filter(m -> !m.isStatic())
                .filter(definition::matches)
                .findFirst()
                .orElseThrow();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        builder.append(definition.returnType());
        builder.append(" ");
        builder.append(definition.name());
        builder.append("(");
        if (definition.parameters().length > 0) {
            // most likely we will not need this
            throw new InternalException();
        }
        builder.append(")");
        builder.append(" }");
        return builder.toString();
    }
}