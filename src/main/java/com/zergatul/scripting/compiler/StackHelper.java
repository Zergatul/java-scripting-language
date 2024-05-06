package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.SWAP;

public class StackHelper {

    public static void duplicate2(MethodVisitor visitor, SType type1, SType type2) {
        if (type1.isJvmCategoryOneComputationalType() && type2.isJvmCategoryOneComputationalType()) {
            visitor.visitInsn(DUP2);
        } else {
            throw new InternalException("Not implemented.");
        }
    }

    public static void swap(MethodVisitor visitor, CompilerContext context, SType type1, SType type2) {
        if (type1.isJvmCategoryOneComputationalType() && type2.isJvmCategoryOneComputationalType()) {
            visitor.visitInsn(SWAP);
        } else {
            context = context.createChild();
            LocalVariable var1 = context.addLocalVariable(null, type1);
            LocalVariable var2 = context.addLocalVariable(null, type2);
            var2.compileStore(context, visitor);
            var1.compileStore(context, visitor);
            var2.compileLoad(context, visitor);
            var1.compileLoad(context, visitor);
        }
    }
}