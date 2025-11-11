package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public class RawTypeSymbol extends Symbol {

    public RawTypeSymbol(String name, SType type) {
        super(name, type, TextRange.MISSING);
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        throw new InternalException();
    }
}