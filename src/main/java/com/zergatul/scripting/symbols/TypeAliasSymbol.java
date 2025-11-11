package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.type.SAliasType;
import org.objectweb.asm.MethodVisitor;

public class TypeAliasSymbol extends Symbol {

    public TypeAliasSymbol(String name, TextRange definition) {
        super(name, new SAliasType(name), definition);
    }

    public SAliasType getAliasType() {
        return (SAliasType) getType();
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        throw new InternalException();
    }
}