package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.CompilerContext;
import org.objectweb.asm.MethodVisitor;

import java.util.function.Consumer;

public class StaticCustomGetterPropertyReference extends PropertyReference {

    private final SType type;
    private final String name;
    private final Consumer<MethodVisitor> getter;

    public StaticCustomGetterPropertyReference(SType type, String name, Consumer<MethodVisitor> getter) {
        this.type = type;
        this.name = name;
        this.getter = getter;
    }

    @Override
    public SType getType() {
        return type;
    }

    @Override
    public boolean canLoad() {
        return true;
    }

    @Override
    public boolean canStore() {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor, Runnable compileCallee) {
        getter.accept(visitor);
    }
}