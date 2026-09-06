package com.zergatul.scripting.type;

import com.zergatul.scripting.utility.Lists;

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
        return Lists.from(MemberLookup.getMethods(underlying).stream().filter(MethodReference::isStatic));
    }

    @Override
    public List<PropertyReference> getDeclaredProperties() {
        return Lists.from(MemberLookup.getProperties(underlying).stream().filter(PropertyReference::isStatic));
    }

    @Override
    public String toString() {
        return underlying.toString();
    }
}