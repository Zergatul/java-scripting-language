package com.zergatul.scripting.compiler;

import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BufferedMethodVisitor extends MethodVisitor {

    private final List<Consumer<MethodVisitor>> buffer = new ArrayList<>();

    public BufferedMethodVisitor() {
        super(589824);
    }

    @Override
    public void visitInsn(int opcode) {
        buffer.add(visitor -> visitor.visitInsn(opcode));
    }

    @Override
    public void visitLdcInsn(Object value) {
        buffer.add(visitor -> visitor.visitLdcInsn(value));
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        buffer.add(visitor -> visitor.visitVarInsn(opcode, varIndex));
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        buffer.add(visitor -> visitor.visitFieldInsn(opcode, owner, name, descriptor));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        buffer.add(visitor -> visitor.visitMethodInsn(opcode, owner, name, descriptor, isInterface));
    }

    public void release(MethodVisitor visitor) {
        for (Consumer<MethodVisitor> consumer : buffer) {
            consumer.accept(visitor);
        }
    }
}