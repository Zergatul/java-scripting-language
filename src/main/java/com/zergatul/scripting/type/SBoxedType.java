package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.CastOperation;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SBoxedType extends SReferenceType {

    private final SValueType underlying;
    private final Class<?> clazz;
    private final Lazy<List<CastOperation>> implicitCasts = new Lazy<>(this::getImplicitCastsInternal);

    protected SBoxedType(SValueType underlying, Class<?> clazz) {
        this.underlying = underlying;
        this.clazz = clazz;
    }

    public static boolean match(SType type1, SType type2) {
        if (type1 instanceof SBoxedType boxed) {
            type1 = boxed.underlying;
        }
        if (type2 instanceof SBoxedType boxed) {
            type2 = boxed.underlying;
        }
        return type1.equals(type2);
    }

    @Override
    public Class<?> getJavaClass() {
        return clazz;
    }

    @Override
    public List<MethodReference> getDeclaredInstanceMethods() {
        return underlying.getDeclaredInstanceMethods().stream()
                .map(method -> new WrappedMethodReference(this, method))
                .map(wrapped -> (MethodReference) wrapped)
                .toList();
    }

    @Override
    public List<UnaryOperation> getUnaryOperations() {
        List<UnaryOperation> operations = new ArrayList<>();
        for (UnaryOperation operation : underlying.getUnaryOperations()) {
            operations.add(new WrapperUnaryOperation(this, operation));
        }
        return operations;
    }

    @Override
    public List<BinaryOperation> getBinaryOperations() {
        List<BinaryOperation> operations = new ArrayList<>();
        for (BinaryOperation operation : underlying.getBinaryOperations()) {
            if (operation.getLeft() instanceof SValueType leftValueType && operation.getRight() instanceof SValueType rightValueType) {
                // convert <value1> <op> <value2> to <boxed1> <op> <boxed2>
                // because binary operation resolver doesn't do implicit casts on both arguments
                operations.add(new BinaryOperation(operation.getOperator(), operation.getResultType(), leftValueType.getBoxed(), rightValueType.getBoxed()) {
                    @Override
                    public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
                        leftValueType.compileUnboxing(left);
                        rightValueType.compileUnboxing(right);
                        operation.apply(left, right, context, leftValueType, rightValueType);
                    }
                });
            }
        }
        return operations;
    }

    @Override
    public List<CastOperation> getImplicitCasts() {
        return implicitCasts.value();
    }

    @Override
    public boolean hasDefaultValue() {
        return false;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        throw new InternalException();
    }

    @Override
    public String toString() {
        return String.format("Boxed<%s>", underlying);
    }

    private List<CastOperation> getImplicitCastsInternal() {
        List<CastOperation> casts = new ArrayList<>();
        casts.add(new UnboxCastOperation(this));
        for (CastOperation cast : underlying.getImplicitCasts()) {
            if (cast.getDstType() != this) {
                casts.add(new WrappedCastOperation(this, cast));
            }
        }
        return Collections.unmodifiableList(casts);
    }

    private static class UnboxCastOperation extends CastOperation {

        private final SBoxedType boxed;

        protected UnboxCastOperation(SBoxedType boxed) {
            super(boxed, boxed.underlying);
            this.boxed = boxed;
        }

        @Override
        public void apply(MethodVisitor visitor) {
            boxed.underlying.compileUnboxing(visitor);
        }
    }

    private static class WrappedCastOperation extends CastOperation {

        private final SBoxedType boxed;
        private final CastOperation underlying;

        protected WrappedCastOperation(SBoxedType boxed, CastOperation underlying) {
            super(boxed, underlying.getDstType());
            this.boxed = boxed;
            this.underlying = underlying;
        }

        @Override
        public void apply(MethodVisitor visitor) {
            boxed.underlying.compileUnboxing(visitor);
            underlying.apply(visitor);
        }
    }

    private static class WrappedMethodReference extends MethodReference {

        private final SBoxedType boxed;
        private final MethodReference underlying;

        public WrappedMethodReference(SBoxedType boxed, MethodReference underlying) {
            this.boxed = boxed;
            this.underlying = underlying;
        }

        @Override
        public SType getOwner() {
            return boxed.underlying;
        }

        @Override
        public SType getReturn() {
            return underlying.getReturn();
        }

        @Override
        public List<MethodParameter> getParameters() {
            return underlying.getParameters();
        }

        @Override
        public void compileInvoke(MethodVisitor visitor, CompilerContext context, Runnable compileArguments) {
            compileArguments.run();
            boxed.underlying.compileUnboxing(visitor);
            underlying.compileInvoke(visitor, context, () -> {});
        }

        @Override
        public String getName() {
            return underlying.getName();
        }

        @Override
        public Optional<String> getDescription() {
            return underlying.getDescription();
        }
    }

    private static class WrapperUnaryOperation extends UnaryOperation {

        private final SBoxedType boxed;
        private final UnaryOperation underlying;

        protected WrapperUnaryOperation(SBoxedType boxed, UnaryOperation underlying) {
            super(underlying.getOperator(), underlying.getResultType(), boxed);
            this.boxed = boxed;
            this.underlying = underlying;
        }

        @Override
        public void apply(MethodVisitor visitor, CompilerContext context) {
            boxed.underlying.compileUnboxing(visitor);
            underlying.apply(visitor, context);
        }
    }
}