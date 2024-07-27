package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;

import java.util.function.Consumer;

public class GetterPropertyReference extends PropertyReference {

    private final SType type;
    private final String name;
    private final Consumer<MethodVisitor> getter;

    public GetterPropertyReference(SType type, String name, Consumer<MethodVisitor> getter) {
        this.type = type;
        this.name = name;
        this.getter = getter;
    }

    @Override
    public SType getType() {
        return type;
    }

    @Override
    public boolean canGet() {
        return true;
    }

    @Override
    public boolean canSet() {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void compileGet(MethodVisitor visitor) {
        getter.accept(visitor);
    }
}