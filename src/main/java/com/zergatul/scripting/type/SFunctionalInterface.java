package com.zergatul.scripting.type;

import com.zergatul.scripting.InterfaceHelper;
import org.objectweb.asm.Type;

import java.lang.reflect.*;
import java.util.Arrays;

public class SFunctionalInterface extends SFunction {

    private final Class<?> clazz;
    private final Method method;
    private final SType rawReturnType;
    private final SType actualReturnType;
    private final SType[] rawParameters;
    private final SType[] actualParameters;

    public static SFunctionalInterface from(Class<?> clazz) {
        Method method = InterfaceHelper.getFuncInterfaceMethod(clazz);
        SType returnType = SType.fromJavaType(method.getReturnType());
        SType[] parameters = Arrays.stream(method.getParameters())
                .map(Parameter::getType)
                .map(SType::fromJavaType)
                .toArray(SType[]::new);
        MethodParameter[] methodParameters = new MethodParameter[parameters.length];
        for (int i = 0; i < methodParameters.length; i++) {
            methodParameters[i] = new MethodParameter(method.getParameters()[i].getName(), parameters[i]);
        }
        return new SFunctionalInterface(
                clazz,
                method,
                returnType,
                returnType,
                parameters,
                parameters,
                methodParameters);
    }

    public static SFunctionalInterface from(ParameterizedType type) {
        Class<?> clazz = (Class<?>) type.getRawType();
        Method method = InterfaceHelper.getFuncInterfaceMethod(clazz);

        TypeVariable<? extends Class<?>>[] classTypeParams = clazz.getTypeParameters();
        java.lang.reflect.Type[] actualArgs = type.getActualTypeArguments();

        SType rawReturnType = SType.fromJavaType(method.getReturnType());
        java.lang.reflect.Type genericReturnType = method.getGenericReturnType();
        SType actualReturnType = SType.fromJavaType(resolveActualType(genericReturnType, classTypeParams, actualArgs));

        SType[] rawParameters = Arrays.stream(method.getParameters())
                .map(Parameter::getType)
                .map(SType::fromJavaType)
                .toArray(SType[]::new);
        SType[] actualParameters = Arrays.stream(method.getGenericParameterTypes())
                .map(t -> resolveActualType(t, classTypeParams, actualArgs))
                .map(SType::fromJavaType)
                .toArray(SType[]::new);

        MethodParameter[] methodParameters = new MethodParameter[actualParameters.length];
        for (int i = 0; i < methodParameters.length; i++) {
            methodParameters[i] = new MethodParameter(method.getParameters()[i].getName(), actualParameters[i]);
        }

        return new SFunctionalInterface(
                clazz,
                method,
                rawReturnType,
                actualReturnType,
                rawParameters,
                actualParameters,
                methodParameters);
    }

    private SFunctionalInterface(
            Class<?> clazz,
            Method method,
            SType rawReturnType,
            SType actualReturnType,
            SType[] rawParameters,
            SType[] actualParameters,
            MethodParameter[] methodParameters
    ) {
        super(actualReturnType, methodParameters);
        this.clazz = clazz;
        this.method = method;
        this.rawReturnType = rawReturnType;
        this.actualReturnType = actualReturnType;
        this.rawParameters = rawParameters;
        this.actualParameters = actualParameters;
    }

    public Method getInterfaceMethod() {
        return method;
    }

    @Override
    public Class<?> getJavaClass() {
        return clazz;
    }

    public SType getRawReturnType() {
        return rawReturnType;
    }

    public SType getActualReturnType() {
        return actualReturnType;
    }

    public SType[] getRawParameters() {
        return rawParameters;
    }

    public SType[] getActualParameters() {
        return actualParameters;
    }

    public String getMethodName() {
        return method.getName();
    }

    public String getRawMethodDescriptor() {
        return Type.getMethodDescriptor(method);
    }

    public String getIntermediateMethodDescriptor() {
        return Type.getMethodDescriptor(
                Type.getType(actualReturnType.getJavaClass()),
                Arrays.stream(getActualParameters()).map(t -> {
                    if (t instanceof SValueType valueType) {
                        return valueType.getBoxed().getDescriptor();
                    } else {
                        return t.getDescriptor();
                    }
                }).map(Type::getType).toArray(Type[]::new));
    }

    public String getActualMethodDescriptor() {
        return getMethodDescriptor();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SFunctionalInterface other) {
            return  other.clazz == clazz &&
                    other.method.equals(method) &&
                    other.actualReturnType.equals(actualReturnType) &&
                    Arrays.equals(other.actualParameters, actualParameters);
        } else if (obj instanceof SClassType other) {
            return other.getJavaClass() == clazz;
        } else {
            return false;
        }
    }

    private static java.lang.reflect.Type resolveActualType(
            java.lang.reflect.Type type,
            TypeVariable<? extends Class<?>>[] classTypeParams,
            java.lang.reflect.Type[] actualArgs
    ) {
        if (type instanceof TypeVariable<?> typeVariable) {
            int index = findTypeParamIndex(classTypeParams, typeVariable.getName());
            if (index != -1) {
                return normalizeWildcard(actualArgs[index]);
            }

            return typeVariable.getBounds()[0];
        }

        return type;
    }

    private static int findTypeParamIndex(TypeVariable<? extends Class<?>>[] params, String name) {
        int index = -1;
        for (int i = 0; i < params.length; i++) {
            if (params[i].getName().equals(name)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private static java.lang.reflect.Type normalizeWildcard(java.lang.reflect.Type type) {
        if (type instanceof WildcardType wildcard) {
            java.lang.reflect.Type[] lowerBounds = wildcard.getLowerBounds();
            if (lowerBounds.length > 0) {
                return normalizeWildcard(lowerBounds[0]);
            }

            return normalizeWildcard(wildcard.getUpperBounds()[0]);
        }

        if (type instanceof TypeVariable<?> typeVariable) {
            return SType.eraseTypeVariableBound(typeVariable);
        }

        return type;
    }
}