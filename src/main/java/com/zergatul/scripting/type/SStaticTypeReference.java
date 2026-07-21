package com.zergatul.scripting.type;

import java.util.List;

public class SStaticTypeReference extends SSyntheticType {

    private final SType underlying;

    public SStaticTypeReference(SType underlying) {
        this.underlying = underlying;
    }

    public SType getUnderlying() {
        return underlying;
    }

    @Override
    public List<MethodReference> getDeclaredMethods() {
        return MemberLookup.getMethods(underlying).stream()
                .filter(MethodReference::isStatic)
                .toList();
    }

    @Override
    public List<PropertyReference> getDeclaredProperties() {
        return MemberLookup.getProperties(underlying).stream()
                .filter(PropertyReference::isStatic)
                .toList();
    }

    @Override
    public String toString() {
        return underlying.toString();
    }
}