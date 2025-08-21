package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.type.SMethodFunction;
import org.objectweb.asm.MethodVisitor;

public class ConstructorSymbol extends Symbol {

    public ConstructorSymbol(SMethodFunction type, TextRange definition) {
        super("", type, definition);
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        throw new InternalException();
    }
}