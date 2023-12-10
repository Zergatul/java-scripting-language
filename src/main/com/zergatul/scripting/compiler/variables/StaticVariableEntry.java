package com.zergatul.scripting.compiler.variables;

import com.zergatul.scripting.compiler.CompilerMethodVisitor;
import com.zergatul.scripting.compiler.types.SType;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class StaticVariableEntry extends VariableEntry {

    private final String className;
    private final String identifier;

    public StaticVariableEntry(SType type, String className, String identifier) {
        super(type);
        this.className = className;
        this.identifier = identifier;
    }

    public String getClassName() {
        return className;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public void compileLoad(CompilerMethodVisitor visitor) {
        visitor.visitFieldInsn(
                GETSTATIC,
                className,
                identifier,
                Type.getDescriptor(type.getJavaClass()));
    }

    @Override
    public void compileStore(CompilerMethodVisitor visitor) {
        visitor.visitFieldInsn(
                PUTSTATIC,
                className,
                identifier,
                Type.getDescriptor(type.getJavaClass()));
    }

    @Override
    public void compileIncrement(CompilerMethodVisitor visitor, int value) {
        compileLoad(visitor);
        visitor.visitLdcInsn(value);
        visitor.visitInsn(IADD);
        compileStore(visitor);
    }
}
