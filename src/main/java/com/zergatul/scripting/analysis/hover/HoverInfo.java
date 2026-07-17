package com.zergatul.scripting.analysis.hover;

import org.jspecify.annotations.Nullable;

public record HoverInfo(String signature, @Nullable String documentation) {

    public HoverInfo(String signature) {
        this(signature, null);
    }
}