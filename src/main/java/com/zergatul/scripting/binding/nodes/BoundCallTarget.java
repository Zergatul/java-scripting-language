package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.type.MethodReference;

public record BoundCallTarget(MethodReference method, DispatchKind dispatch, AccessStrategy access) {

    public enum DispatchKind {
        NORMAL,
        BASE
    }

    public enum AccessStrategy {
        DIRECT,
        METHOD_HANDLE
    }
}