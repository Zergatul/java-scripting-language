package com.zergatul.scripting.analysis.hover;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class HoverInfo {

    private final String signature;
    private final @Nullable String documentation;

    public HoverInfo(String signature, @Nullable String documentation) {
        this.signature = signature;
        this.documentation = documentation;
    }

    public HoverInfo(String signature) {
        this(signature, null);
    }

    public String signature() {
        return signature;
    }

    public @Nullable String documentation() {
        return documentation;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        HoverInfo that = (HoverInfo) obj;
        return  Objects.equals(this.signature, that.signature) &&
                Objects.equals(this.documentation, that.documentation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signature, documentation);
    }

    @Override
    public String toString() {
        return  "HoverInfo[" +
                "signature=" + signature + ", " +
                "documentation=" + documentation + ']';
    }
}