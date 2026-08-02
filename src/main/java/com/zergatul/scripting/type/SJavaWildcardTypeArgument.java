package com.zergatul.scripting.type;

import java.util.List;

public record SJavaWildcardTypeArgument(List<SType> upperBounds, List<SType> lowerBounds) implements SJavaTypeArgument {

    public static SJavaWildcardTypeArgument extendsType(SType type) {
        return new SJavaWildcardTypeArgument(List.of(type), List.of());
    }

    public static SJavaWildcardTypeArgument superType(SType type) {
        return new SJavaWildcardTypeArgument(List.of(SJavaObject.instance), List.of(type));
    }
}