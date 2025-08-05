package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.type.SDeclaredType;
import org.objectweb.asm.MethodVisitor;

public class ClassSymbol extends Symbol {

    private final SDeclaredType type;

    public ClassSymbol(String name, SDeclaredType type, TextRange definition) {
        super(name, type, definition);
        this.type = type;
    }

    public SDeclaredType getDeclaredType() {
        return type;
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        throw new InternalException();
    }
}