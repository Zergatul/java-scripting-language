package com.zergatul.scripting.symbols;

import com.zergatul.scripting.type.SType;

public class ThisLocalVariable extends LocalVariable {
    public ThisLocalVariable(SType type) {
        super("_this", type, null);
    }
}