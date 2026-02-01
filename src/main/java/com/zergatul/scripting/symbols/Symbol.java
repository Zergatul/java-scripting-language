package com.zergatul.scripting.symbols;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.type.SType;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;

public abstract class Symbol {

    private final @Nullable String name;
    private final SType type;

    private final @Nullable TextRange definition;

    protected Symbol(@Nullable String name, SType type, @Nullable TextRange definition) {
        this.name = name;
        this.type = type;
        this.definition = definition;
    }

    public SType getType() {
        return type;
    }

    public @Nullable String getName() {
        return name;
    }

    public @Nullable TextRange getDefinition() {
        return definition;
    }

    public boolean canSet() {
        return false;
    }

    public abstract void compileLoad(CompilerContext context, MethodVisitor visitor);
}