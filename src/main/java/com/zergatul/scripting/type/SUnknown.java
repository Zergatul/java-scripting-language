package com.zergatul.scripting.type;

import java.util.List;

public class SUnknown extends SSyntheticType {

    public static final SUnknown instance = new SUnknown();

    private SUnknown() {}

    @Override
    public List<ConstructorReference> getConstructors() {
        return List.of(UnknownConstructorReference.instance);
    }

    @Override
    public List<MethodReference> getInstanceMethods() {
        return List.of(UnknownMethodReference.instance);
    }

    @Override
    public String toString() {
        return "<Unknown>";
    }
}