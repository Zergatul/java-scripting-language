package com.zergatul.scripting.compiler;

import com.zergatul.scripting.compiler.types.SType;
import com.zergatul.scripting.compiler.types.SVoidType;
import com.zergatul.scripting.compiler.variables.VariableContextStack;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class MethodVisitorWrapper extends CompilerMethodVisitor {

    private final MethodVisitor visitor;
    private final String className;
    private final VariableContextStack contexts;
    private final LoopContextStack loops;
    private final SType returnType;

    public MethodVisitorWrapper(MethodVisitor visitor, String className, VariableContextStack contexts) {
        this(visitor, className, contexts, SVoidType.instance);
    }

    public MethodVisitorWrapper(MethodVisitor visitor, String className, VariableContextStack contexts, SType returnType) {
        this.visitor = visitor;
        this.className = className;
        this.contexts = contexts;
        this.loops = new LoopContextStack();
        this.returnType = returnType;
    }

    @Override
    public String getClassName() {
        return this.className;
    }

    @Override
    public VariableContextStack getContextStack() {
        return contexts;
    }

    @Override
    public LoopContextStack getLoops() {
        return loops;
    }

    @Override
    public SType getReturnType() {
        return returnType;
    }

    @Override
    public void visitInsn(int opcode) {
        visitor.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        visitor.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        visitor.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        visitor.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(Label label) {
        visitor.visitLabel(label);
    }

    @Override
    public void visitLdcInsn(Object value) {
        visitor.visitLdcInsn(value);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        visitor.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitTypeInsn(int opcode, String descriptor) {
        visitor.visitTypeInsn(opcode, descriptor);
    }

    @Override
    public void visitVarInsn(int opcode, int index) {
        visitor.visitVarInsn(opcode, index);
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        visitor.visitIincInsn(varIndex, increment);
    }
}