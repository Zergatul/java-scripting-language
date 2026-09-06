package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.type.MethodReference;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class BoundCallTarget {

    private final MethodReference method;
    private final DispatchKind dispatch;
    private final AccessStrategy access;

    public BoundCallTarget(MethodReference method, DispatchKind dispatch, AccessStrategy access) {
        this.method = method;
        this.dispatch = dispatch;
        this.access = access;
    }

    public MethodReference method() {
        return method;
    }

    public DispatchKind dispatch() {
        return dispatch;
    }

    public AccessStrategy access() {
        return access;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BoundCallTarget that = (BoundCallTarget) obj;
        return  Objects.equals(this.method, that.method) &&
                Objects.equals(this.dispatch, that.dispatch) &&
                Objects.equals(this.access, that.access);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, dispatch, access);
    }

    @Override
    public String toString() {
        return  "BoundCallTarget[" +
                "method=" + method + ", " +
                "dispatch=" + dispatch + ", " +
                "access=" + access + ']';
    }

    public enum DispatchKind {
        NORMAL,
        BASE
    }

    public enum AccessStrategy {
        DIRECT,
        METHOD_HANDLE
    }
}