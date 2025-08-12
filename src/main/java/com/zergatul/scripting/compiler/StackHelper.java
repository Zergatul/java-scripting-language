package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.symbols.LocalVariable;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.SWAP;

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

    public static void swap(MethodVisitor visitor, CompilerContext context, SType type1, SType type2) {
        if (type1.isJvmCategoryOneComputationalType() && type2.isJvmCategoryOneComputationalType()) {
            visitor.visitInsn(SWAP);
        } else {
            context = context.createChild();
            SymbolRef var1 = context.addLocalVariable(null, type1, null);
            SymbolRef var2 = context.addLocalVariable(null, type2, null);
            context.setStackIndex(var1.asLocalVariable());
            context.setStackIndex(var2.asLocalVariable());
            var2.asVariable().compileStore(context, visitor);
            var1.asVariable().compileStore(context, visitor);
            var2.asVariable().compileLoad(context, visitor);
            var1.asVariable().compileLoad(context, visitor);
        }
    }
}