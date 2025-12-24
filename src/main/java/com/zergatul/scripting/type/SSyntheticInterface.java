package com.zergatul.scripting.type;

public class SSyntheticInterface extends SSyntheticType {

    private final MethodDefinition definition;

    public SSyntheticInterface(MethodDefinition definition) {
        this.definition = definition;
    }

    @Override
    public boolean isCompatibleWith(SType other) {
        return other.getInstanceMethods().stream().anyMatch(definition::matches);
    }

    public MethodReference extractMethod(SType other) {
        return other.getInstanceMethods().stream().filter(definition::matches).findFirst().orElseThrow();
    }
}