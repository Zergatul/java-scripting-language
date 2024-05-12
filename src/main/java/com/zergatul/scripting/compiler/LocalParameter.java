package com.zergatul.scripting.compiler;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.type.SType;

public class LocalParameter extends LocalVariable {
    public LocalParameter(String name, SType type, int stackIndex, TextRange definition) {
        super(name, type, stackIndex, definition);
    }
}