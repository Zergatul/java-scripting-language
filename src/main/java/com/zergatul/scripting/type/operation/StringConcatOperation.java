package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.type.SString;
import com.zergatul.scripting.type.SStringConvertible;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.utility.Lists;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class StringConcatOperation extends BinaryOperation {

    public static final int MAX_INDY_CONCAT_ARG_SLOTS = 200;
    public static final String STRING_UTILS_CLASS_NAME = "com/zergatul/scripting/dynamic/strings/StringUtil";
    public static final String CONCANT_METHOD_NAME = "concat";

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
        compileInvokeDynamic(context, left, Lists.of(effectiveLeftType, effectiveRightType));
    }

    public static void compileInvokeDynamic(CompilerContext context, MethodVisitor visitor, List<SType> types) {
        context.requestStringConcat(types);
        visitor.visitMethodInsn(
                INVOKESTATIC,
                STRING_UTILS_CLASS_NAME,
                CONCANT_METHOD_NAME,
                Type.getMethodDescriptor(Type.getType(String.class), types.stream().map(SType::getAsmType).toArray(Type[]::new)),
                false);
    }

    private static SType compileConversion(MethodVisitor visitor, CompilerContext context, SType type, boolean convert) {
        if (convert) {
            SStringConvertible.instance.extractMethod(type).compileInvoke(visitor, context, innerContext -> {});
            return SString.instance;
        } else {
            return type;
        }
    }
}