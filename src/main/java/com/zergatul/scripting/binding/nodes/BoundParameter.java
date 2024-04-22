package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.type.SType;

public class BoundParameter {

    private final String identifier;
    private final SType type;

    public BoundParameter(String identifier, SType type) {
        this.identifier = identifier;
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public SType getType() {
        return type;
    }
}