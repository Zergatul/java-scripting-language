package com.zergatul.scripting.compiler;

import org.objectweb.asm.ClassWriter;

final class CompilerClassWriter extends ClassWriter {

    private final TypeHierarchy hierarchy;

    public CompilerClassWriter(TypeHierarchy hierarchy) {
        super(COMPUTE_FRAMES);
        this.hierarchy = hierarchy;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        return hierarchy.getCommonSuperClass(type1, type2);
    }
}