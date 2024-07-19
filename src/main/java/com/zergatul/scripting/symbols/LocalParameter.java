package com.zergatul.scripting.symbols;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.type.SType;

public class LocalParameter extends LocalVariable {
    public LocalParameter(String name, SType type, TextRange definition) {
        super(name, type, definition);
    }
}