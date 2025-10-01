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
    public List<MethodReference> getInstanceMethods() {
        return underlying.getStaticMethods();
    }

    @Override
    public List<PropertyReference> getInstanceProperties() {
        return underlying.getStaticProperties();
    }
}