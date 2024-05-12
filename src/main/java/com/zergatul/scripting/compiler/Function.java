package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.type.SFunction;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public class Function extends Symbol {

    public Function(String name, SFunction type, TextRange definition) {
        super(name, type, definition);
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        throw new InternalException();
    }

    public SFunction getFunctionType() {
        return (SFunction) getType();
    }
}