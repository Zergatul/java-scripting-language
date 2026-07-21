package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.CompilerContext;
import org.objectweb.asm.MethodVisitor;

import java.util.Optional;

public abstract class PropertyReference extends MemberReference {

    public abstract SType getType();
    public abstract boolean canLoad();
    public abstract boolean canStore();

    public Optional<String> getDescription() {
        return Optional.empty();
    }

    public void compileLoad(MethodVisitor visitor, CompilerContext context, Runnable compileCallee) {
        throw new InternalException();
    }

    public void compileStore(MethodVisitor visitor, CompilerContext context, Runnable compileCallee, Runnable compileValue) {
        throw new InternalException();
    }

    public void compileLoadModifyStore(MethodVisitor visitor, CompilerContext context, Runnable compileCallee, Runnable compileModify) {
        throw new InternalException();
    }
}