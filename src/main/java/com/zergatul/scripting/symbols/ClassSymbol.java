package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public class ClassSymbol extends Symbol {

    public ClassSymbol(String name, SType type, TextRange definition) {
        super(name, type, definition);
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        throw new InternalException();
    }
}