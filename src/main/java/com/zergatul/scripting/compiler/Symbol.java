package com.zergatul.scripting.compiler;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public abstract class Symbol {

    private final String name;
    private final SType type;
    private final TextRange definition;

    protected Symbol(String name, SType type) {
        this(name, type, null);
    }

    protected Symbol(String name, SType type, TextRange definition) {
        this.name = name;
        this.type = type;
        this.definition = definition;
    }

    public SType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public TextRange getDefinition() {
        return definition;
    }

    public boolean canSet() {
        return false;
    }

    public abstract void compileLoad(CompilerContext context, MethodVisitor visitor);
}