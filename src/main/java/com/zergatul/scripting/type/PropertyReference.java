package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.CompilerContext;
import org.objectweb.asm.MethodVisitor;

public abstract class PropertyReference extends MemberReference {

    public abstract SType getType();
    public abstract boolean canLoad();
    public abstract boolean canStore();

    public boolean isPublic() {
        return true;
    }

    public void compileLoad(CompilerContext context, MethodVisitor visitor, Runnable compileCallee) {
        throw new InternalException();
    }

    public void compileStore(CompilerContext context, MethodVisitor visitor, Runnable compileCallee, Runnable compileValue) {
        throw new InternalException();
    }

    public void compileLoadModifyStore(CompilerContext context, MethodVisitor visitor, Runnable compileCallee, Runnable compileModify) {
        throw new InternalException();
    }
}