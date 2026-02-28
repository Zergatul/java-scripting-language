package com.zergatul.scripting.generator;

public enum GeneratorStackEntryType {
    FINALLY(1),
    BREAK(2),
    CONTINUE(3);

    private final int internalIndex;

    GeneratorStackEntryType(int internalIndex) {
        this.internalIndex = internalIndex;
    }

    public int getInternalIndex() {
        return internalIndex;
    }
}