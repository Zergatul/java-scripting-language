package com.zergatul.scripting.symbols;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.type.SType;

public class LocalParameter extends LocalVariable {
    public LocalParameter(String name, SType type, int stackIndex, CompilerContext functionContext, TextRange definition) {
        super(name, type, stackIndex, functionContext, definition);
    }
}