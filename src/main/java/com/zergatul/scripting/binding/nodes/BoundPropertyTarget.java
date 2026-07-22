package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.type.PropertyReference;

public record BoundPropertyTarget(PropertyReference property, AccessStrategy access) {

    public enum AccessStrategy {
        DIRECT,
        VAR_HANDLE
    }
}