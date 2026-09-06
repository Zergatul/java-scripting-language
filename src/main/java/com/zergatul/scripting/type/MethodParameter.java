package com.zergatul.scripting.type;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class MethodParameter {

    private final String name;
    private final SType type;

    public MethodParameter(String name, SType type) {
        this.name = name;
        this.type = type;
    }

    public String name() {
        return name;
    }

    public SType type() {
        return type;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        MethodParameter that = (MethodParameter) obj;
        return  Objects.equals(this.name, that.name) &&
                Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return  "MethodParameter[" +
                "name=" + name + ", " +
                "type=" + type + ']';
    }
}