package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class StackHelper {

    public static int[] buildStackIndexes(SType[] parameters) {
        int[] indexes = new int[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            indexes[i] = i == 0 ? 1 : indexes[i - 1] + (parameters[i - 1].isJvmCategoryOneComputationalType() ? 1 : 2);
        }
        return indexes;
    }

    public static void duplicate2(MethodVisitor visitor, SType type1, SType type2) {
        if (type1.isJvmCategoryOneComputationalType() && type2.isJvmCategoryOneComputationalType()) {
            visitor.visitInsn(DUP2);
        } else {
            throw new InternalException("Not implemented.");
        }
    }

    public static void swap(MethodVisitor visitor, SType type1, SType type2) {
        if (type1.isJvmCategoryOneComputationalType()) {
            if (type2.isJvmCategoryOneComputationalType()) {
                visitor.visitInsn(SWAP);
            } else {
                visitor.visitInsn(DUP2_X1);
                visitor.visitInsn(POP2);
            }
        } else {
            if (type2.isJvmCategoryOneComputationalType()) {
                visitor.visitInsn(DUP_X2);
                visitor.visitInsn(POP);
            } else {
                visitor.visitInsn(DUP2_X2);
                visitor.visitInsn(POP2);
            }
        }
    }
}