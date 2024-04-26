package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.SFunction;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public class Function extends Symbol {

    public Function(String name, SFunction type) {
        super(name, type);
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        throw new InternalException();
    }
}