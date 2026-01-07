package com.zergatul.scripting.type;

import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.CastOperation;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SChar extends SValueType {

    public static final SChar instance = new SChar();

    private final SBoxedType boxed = new SBoxedType(this, Character.class);
    private final Lazy<List<BinaryOperation>> binaryOperations = new Lazy<>(this::getBinaryOperationsInternal);
    private final Lazy<List<CastOperation>> implicitCasts = new Lazy<>(this::getImplicitCastsInternal);

    private SChar() {
        super(char.class);
    }

    @Override
    public int getLoadInst() {
        return ILOAD;
    }

    @Override
    public int getStoreInst() {
        return ISTORE;
    }

    @Override
    public boolean hasDefaultValue() {
        return true;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        visitor.visitLdcInsn((char) 0);
    }

    @Override
    public int getArrayTypeInst() {
        return T_CHAR;
    }

    @Override
    public int getArrayLoadInst() {
        return CALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return CASTORE;
    }

    @Override
    public int getReturnInst() {
        return IRETURN;
    }

    @Override
    public SBoxedType getBoxed() {
        return boxed;
    }

    @Override
    public void compileBoxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(Character.class),
                "valueOf",
                Type.getMethodDescriptor(Type.getType(Character.class), Type.CHAR_TYPE),
                false);
    }

    @Override
    public void compileUnboxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(Character.class),
                "charValue",
                Type.getMethodDescriptor(Type.CHAR_TYPE),
                false);
    }

    @Override
    public void loadClassObject(MethodVisitor visitor) {
        visitor.visitFieldInsn(GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
    }

    @Override
    public List<MethodReference> getDeclaredInstanceMethods() {
        return List.of(METHOD_TO_STRING.value());
    }

    @Override
    public List<BinaryOperation> getBinaryOperations() {
        return binaryOperations.value();
    }

    private List<BinaryOperation> getBinaryOperationsInternal() {
        return List.of(
                LESS_THAN.value(),
                GREATER_THAN.value(),
                LESS_THAN_EQUALS.value(),
                GREATER_THAN_EQUALS.value(),
                EQUALS.value(),
                NOT_EQUALS.value());
    }

    @Override
    public List<CastOperation> getImplicitCasts() {
        return implicitCasts.value();
    }

    private List<CastOperation> getImplicitCastsInternal() {
        return extendWithBoxing(
                CHAR_TO_INT.value(),
                CHAR_TO_BOXED.value());
    }

    @Override
    public String toString() {
        return "char";
    }

    private static final Lazy<CastOperation> CHAR_TO_INT = new Lazy<>(() -> new CastOperation(instance, SInt.instance) {
        @Override
        public void apply(MethodVisitor visitor) {}
    });

    private static final Lazy<CastOperation> CHAR_TO_BOXED = new Lazy<>(() -> new CastOperation(instance, instance.boxed) {
        @Override
        public void apply(MethodVisitor visitor) {
            SChar.instance.compileBoxing(visitor);
        }
    });

    protected static final Lazy<BinaryOperation> LESS_THAN = new Lazy<>(() ->
            new CharComparisonOperation(BinaryOperator.LESS, IF_ICMPLT));

    protected static final Lazy<BinaryOperation> GREATER_THAN = new Lazy<>(() ->
            new CharComparisonOperation(BinaryOperator.GREATER, IF_ICMPGT));

    protected static final Lazy<BinaryOperation> LESS_THAN_EQUALS = new Lazy<>(() ->
            new CharComparisonOperation(BinaryOperator.LESS_EQUALS, IF_ICMPLE));

    public static final Lazy<BinaryOperation> GREATER_THAN_EQUALS = new Lazy<>(() ->
            new CharComparisonOperation(BinaryOperator.GREATER_EQUALS, IF_ICMPGE));

    protected static final Lazy<BinaryOperation> EQUALS = new Lazy<>(() ->
            new CharComparisonOperation(BinaryOperator.EQUALS, IF_ICMPEQ));

    protected static final Lazy<BinaryOperation> NOT_EQUALS = new Lazy<>(() ->
            new CharComparisonOperation(BinaryOperator.NOT_EQUALS, IF_ICMPNE));

    private static final Lazy<MethodReference> METHOD_TO_STRING = new Lazy<>(() -> new StaticAsInstanceMethodReference(
            """
                    Returns a string containing single character
                    """,
            String.class,
            SChar.instance,
            "valueOf",
            "toString",
            SString.instance));

    private static class CharComparisonOperation extends BinaryOperation {

        private final int opcode;

        public CharComparisonOperation(BinaryOperator operator, int opcode) {
            super(operator, SBoolean.instance, SChar.instance, SChar.instance);
            this.opcode = opcode;
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
            right.release(left);
            Label elseLabel = new Label();
            Label endLabel = new Label();
            left.visitJumpInsn(opcode, elseLabel);
            left.visitInsn(ICONST_0);
            left.visitJumpInsn(GOTO, endLabel);
            left.visitLabel(elseLabel);
            left.visitInsn(ICONST_1);
            left.visitLabel(endLabel);
        }
    }
}