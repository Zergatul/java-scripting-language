package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.compiler.DynamicCompilerClassLoader;
import com.zergatul.scripting.compiler.StackHelper;
import com.zergatul.scripting.type.SFloat;
import com.zergatul.scripting.type.SInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.*;

public class StackHelperTests {

    @Test
    public void swapCategoryOneAndTwoTest() throws ReflectiveOperationException {
        double result = execute(visitor -> {
            visitor.visitLdcInsn(3);
            visitor.visitLdcInsn(7.5);
            StackHelper.swap(visitor, SInt.instance, SFloat.instance);
            // Stack: 7.5 (double), 3 (int).
            visitor.visitInsn(I2D);
            visitor.visitInsn(DSUB);
        });

        Assertions.assertEquals(4.5, result);
    }

    @Test
    public void swapCategoryTwoAndTwoTest() throws ReflectiveOperationException {
        double result = execute(visitor -> {
            visitor.visitLdcInsn(3.0);
            visitor.visitLdcInsn(7.5);
            StackHelper.swap(visitor, SFloat.instance, SFloat.instance);
            // Stack: 7.5 (double), 3.0 (double).
            visitor.visitInsn(DSUB);
        });

        Assertions.assertEquals(4.5, result);
    }

    private static double execute(Consumer<MethodVisitor> body) throws ReflectiveOperationException {
        String name = "com/zergatul/scripting/dynamic/StackHelperTest";
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        writer.visit(V21, ACC_PUBLIC, name, null, "java/lang/Object", null);

        MethodVisitor visitor = writer.visitMethod(ACC_PUBLIC | ACC_STATIC, "run", "()D", null, null);
        visitor.visitCode();
        body.accept(visitor);
        visitor.visitInsn(DRETURN);
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
        writer.visitEnd();

        Class<?> testClass = new DynamicCompilerClassLoader()
                .defineClass(name.replace('/', '.'), writer.toByteArray());
        return (double) testClass.getMethod("run").invoke(null);
    }
}