package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.type.PropertyReference;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class BoundPropertyTarget {

    private final PropertyReference property;
    private final AccessStrategy access;

    public BoundPropertyTarget(PropertyReference property, AccessStrategy access) {
        this.property = property;
        this.access = access;
    }

    public PropertyReference property() {
        return property;
    }

    public AccessStrategy access() {
        return access;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BoundPropertyTarget that = (BoundPropertyTarget) obj;
        return  Objects.equals(this.property, that.property) &&
                Objects.equals(this.access, that.access);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, access);
    }

    @Override
    public String toString() {
        return  "BoundPropertyTarget[" +
                "property=" + property + ", " +
                "access=" + access + ']';
    }

    public enum AccessStrategy {
        DIRECT,
        VAR_HANDLE
    }
}