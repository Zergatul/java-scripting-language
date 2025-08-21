package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.type.Invocable;
import com.zergatul.scripting.type.MethodParameter;
import com.zergatul.scripting.type.SStaticFunction;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class Function extends Symbol implements Invocable {

    public Function(String name, SStaticFunction type, TextRange definition) {
        super(name, type, definition);
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        throw new InternalException();
    }

    public SStaticFunction getFunctionType() {
        return (SStaticFunction) getType();
    }

    @Override
    public List<MethodParameter> getParameters() {
        return getFunctionType().getParameters();
    }
}