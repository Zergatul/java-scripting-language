package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public abstract class MethodReference extends MemberReference {
    public abstract SType getReturn();
    public abstract List<SType> getParameters();
    public abstract void compileInvoke(MethodVisitor visitor);
}