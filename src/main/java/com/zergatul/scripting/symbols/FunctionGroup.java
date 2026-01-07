package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.type.SUnknown;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

public class FunctionGroup extends Symbol {

    private final List<Function> functions;

    public FunctionGroup(String name, TextRange definition) {
        super(name, SUnknown.instance, definition);
        this.functions = new ArrayList<>();
    }

    public void addFunction(Function function) {
        functions.add(function);
    }

    public List<Function> getFunctions() {
        return functions;
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        throw new InternalException();
    }
}