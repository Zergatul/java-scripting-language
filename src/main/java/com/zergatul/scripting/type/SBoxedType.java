package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.CastOperation;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SBoxedType extends SReferenceType {

    private final SValueType underlying;
    private final Class<?> clazz;

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
    public List<MethodReference> getInstanceMethods() {
        return underlying.getInstanceMethods().stream()
                .map(method -> new WrappedMethodReference(this, method))
                .map(wrapped -> (MethodReference) wrapped)
                .toList();
    }

    @Override
    public @Nullable UnaryOperation unary(UnaryOperator operator) {
        UnaryOperation operation = underlying.unary(operator);
        if (operation != null) {
            return new WrapperUnaryOperation(this, operation);
        }
        return super.unary(operator);
    }

    @Override
    public @Nullable BinaryOperation binary(BinaryOperator operator, SType other) {
        if (other.equals(this)) {
            // <boxed> <op> <boxed>
            BinaryOperation operation = underlying.binary(operator, underlying);
            if (operation != null) {
                return new WrappedBinaryOperation(this, operation, true, true);
            }
        }
        if (other.equals(underlying)) {
            // <boxed> <op> <underlying>
            BinaryOperation operation = underlying.binary(operator, underlying);
            if (operation != null) {
                return new WrappedBinaryOperation(this, operation, true, false);
            }
        }
        return super.binary(operator, other);
    }

    @Override
    public List<SType> getPossibleImplicitCasts() {
        List<SType> result = new ArrayList<>(super.getPossibleImplicitCasts());
        result.add(underlying);
        return result;
    }

    @Override
    protected @Nullable CastOperation implicitCastTo(SType other) {
        if (other.equals(underlying)) {
            return new UnboxCastOperation(this);
        } else {
            CastOperation cast = underlying.implicitCastTo(other);
            if (cast != null) {
                return new WrappedCastOperation(this, cast);
            }
            return null;
        }
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

    private static class UnboxCastOperation extends CastOperation {

        private final SBoxedType boxed;

        protected UnboxCastOperation(SBoxedType boxed) {
            super(boxed.underlying);
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
            super(underlying.type);
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
        public void compileInvoke(MethodVisitor visitor, CompilerContext context) {
            boxed.underlying.compileUnboxing(visitor);
            underlying.compileInvoke(visitor, context);
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
            super(underlying.operator, underlying.type);
            this.boxed = boxed;
            this.underlying = underlying;
        }

        @Override
        public void apply(MethodVisitor visitor) {
            boxed.underlying.compileUnboxing(visitor);
            underlying.apply(visitor);
        }
    }

    private static class WrappedBinaryOperation extends BinaryOperation {

        private final SBoxedType boxed;
        private final BinaryOperation underlying;
        private final boolean castRight;
        private final boolean castLeft;

        protected WrappedBinaryOperation(SBoxedType boxed, BinaryOperation underlying, boolean castLeft, boolean castRight) {
            super(underlying.operator, underlying.type, underlying.getLeft(), underlying.getRight());
            this.boxed = boxed;
            this.underlying = underlying;
            this.castLeft = castLeft;
            this.castRight = castRight;
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context) {
            if (castLeft) {
                boxed.underlying.compileUnboxing(left);
            }
            if (castRight) {
                boxed.underlying.compileUnboxing(right);
            }
            underlying.apply(left, right, context);
        }
    }
}