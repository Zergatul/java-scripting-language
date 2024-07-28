package com.zergatul.scripting.symbols;

import com.zergatul.scripting.type.SType;

public class ExternalParameter extends LocalParameter {

    private final int index;

    public ExternalParameter(String name, SType type, int index) {
        super(name, type, null);
        this.index = index;
        this.setStackIndex(1 + index);
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean canSet() {
        return false;
    }
}