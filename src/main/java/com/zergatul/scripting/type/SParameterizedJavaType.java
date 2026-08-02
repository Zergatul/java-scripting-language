package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;

public class SParameterizedJavaType extends SReferenceType {

    private final Class<?> rawType;
    private final List<SJavaTypeArgument> arguments;
    private final GenericSubstitution substitution;

    public SParameterizedJavaType(Class<?> rawType, List<SJavaTypeArgument> arguments) {
        if (rawType.getTypeParameters().length != arguments.size()) {
            throw new InternalException();
        }

        this.rawType = rawType;
        this.arguments = arguments;
        this.substitution = GenericSubstitution.forClass(rawType, arguments);
    }

    public static SParameterizedJavaType from(ParameterizedType parameterized) {
        Class<?> rawType = (Class<?>) parameterized.getRawType();
        List<SJavaTypeArgument> arguments = Arrays.stream(parameterized.getActualTypeArguments())
                .map(SJavaTypeArgument::from)
                .toList();
        return new SParameterizedJavaType(rawType, arguments);
    }

    @Override
    public Class<?> getJavaClass() {
        return rawType;
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
    public List<ConstructorReference> getConstructors() {
        return Arrays.stream(rawType.getDeclaredConstructors())
                .filter(c -> !c.isSynthetic())
                .map(NativeConstructorReference::new)
                .map(c -> (ConstructorReference) c)
                .toList();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SType.fromJavaType(rawType));
        sb.append('<');
        for (int i = 0; i < arguments.size(); i++) {
            sb.append(switch (arguments.get(i)) {
                case SJavaUnboundedTypeArgument unbounded -> "?";
                case SJavaExactTypeArgument exact -> exact.type().toString();
                case SJavaWildcardTypeArgument wildcard -> throw new InternalException(); //wildcard.type().toString();
                default -> throw new InternalException();
            });
            if (i < arguments.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append('>');
        return sb.toString();
    }
}