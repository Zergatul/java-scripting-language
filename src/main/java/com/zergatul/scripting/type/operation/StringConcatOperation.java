package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.type.SString;
import com.zergatul.scripting.type.SStringConvertible;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.StringConcatFactory;
import java.util.List;

import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

public class StringConcatOperation extends BinaryOperation {

    public static final int MAX_INDY_CONCAT_ARG_SLOTS = 200;

    private static final Handle BOOTSTRAP = new Handle(
            H_INVOKESTATIC,
            Type.getInternalName(StringConcatFactory.class),
            "makeConcat",
            Type.getMethodDescriptor(
                    Type.getType(CallSite.class),
                    Type.getType(MethodHandles.Lookup.class),
                    Type.getType(String.class),
                    Type.getType(MethodType.class)),
            false);

    private final boolean convertLeft;
    private final boolean convertRight;

    public StringConcatOperation(SType left, SType right, boolean convertLeft, boolean convertRight) {
        super(BinaryOperator.PLUS, SString.instance, left, right);
        this.convertLeft = convertLeft;
        this.convertRight = convertRight;
    }

    public SType compileLeftConversion(MethodVisitor visitor, CompilerContext context, SType type) {
        return compileConversion(visitor, context, type, convertLeft);
    }

    public SType compileRightConversion(MethodVisitor visitor, CompilerContext context, SType type) {
        return compileConversion(visitor, context, type, convertRight);
    }

    @Override
    public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
        SType effectiveLeftType = compileLeftConversion(left, context, leftType);
        SType effectiveRightType = compileRightConversion(right, context, rightType);
        right.release(left);
        compileInvokeDynamic(left, List.of(effectiveLeftType, effectiveRightType));
    }

    public static void compileInvokeDynamic(MethodVisitor visitor, List<SType> types) {
        Type[] arguments = types.stream()
                .map(type -> Type.getType(type.getDescriptor()))
                .toArray(Type[]::new);
        visitor.visitInvokeDynamicInsn(
                "makeConcat",
                Type.getMethodDescriptor(Type.getType(String.class), arguments),
                BOOTSTRAP);
    }

    private static SType compileConversion(MethodVisitor visitor, CompilerContext context, SType type, boolean convert) {
        if (convert) {
            SStringConvertible.instance.extractMethod(type).compileInvoke(visitor, context, () -> {});
            return SString.instance;
        } else {
            return type;
        }
    }
}