package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.DUP2;

public class StackHelper {

    public static void duplicate2(MethodVisitor visitor, SType type1, SType type2) {
        if (type1.isJvmCategoryOneComputationalType() && type2.isJvmCategoryOneComputationalType()) {
            visitor.visitInsn(DUP2);
        } else {
            throw new InternalException("Not implemented.");
        }
    }
}