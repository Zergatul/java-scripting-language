package com.zergatul.scripting.compiler;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.type.SType;

public abstract class StaticVariable extends Variable {
    protected StaticVariable(String name, SType type, TextRange definition) {
        super(name, type, definition);
    }
}