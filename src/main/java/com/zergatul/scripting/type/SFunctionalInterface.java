package com.zergatul.scripting.type;

import com.zergatul.scripting.InterfaceHelper;
import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.StackHelper;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

public class SFunctionalInterface extends SType {

    private final Class<?> clazz;
    private final Method method;
    private final SType rawReturnType;
    private final SType actualReturnType;
    private final SType[] rawParameters;
    private final SType[] actualParameters;
    private final int[] paramStackIndexes;

    public SFunctionalInterface(Class<?> clazz) {
        this.clazz = clazz;
        this.method = InterfaceHelper.getFuncInterfaceMethod(clazz);
        this.rawReturnType = this.actualReturnType = SType.fromJavaType(method.getReturnType());
        this.rawParameters = this.actualParameters = Arrays.stream(method.getParameters())
                .map(Parameter::getType)
                .map(SType::fromJavaType)
                .toArray(SType[]::new);
        this.paramStackIndexes = StackHelper.buildStackIndexes(rawParameters);
    }

    public SFunctionalInterface(ParameterizedType type) {
        this.clazz = (Class<?>) type.getRawType();
        this.method = InterfaceHelper.getFuncInterfaceMethod(clazz);

        TypeVariable<? extends Class<?>>[] classTypeParams = clazz.getTypeParameters();
        java.lang.reflect.Type[] actualArgs = type.getActualTypeArguments();

        this.rawReturnType = SType.fromJavaType(method.getReturnType());
        java.lang.reflect.Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof TypeVariable<?> typeVariable) {
            this.actualReturnType = SType.fromJavaType(actualArgs[findTypeParamIndex(classTypeParams, typeVariable.getName())]);
        } else {
            this.actualReturnType = SType.fromJavaType(genericReturnType);
        }

        this.rawParameters = Arrays.stream(method.getParameters())
                .map(Parameter::getType)
                .map(SType::fromJavaType)
                .toArray(SType[]::new);
        this.actualParameters = Arrays.stream(method.getGenericParameterTypes())
                .map(t -> {
                    if (t instanceof TypeVariable<?> typeVariable) {
                        return (Class<?>) actualArgs[findTypeParamIndex(classTypeParams, typeVariable.getName())];
                    } else {
                        return (Class<?>) t;
                    }
                })
                .map(SType::fromJavaType)
                .toArray(SType[]::new);

        this.paramStackIndexes = StackHelper.buildStackIndexes(rawParameters);
    }

    @SuppressWarnings("unused") // for monaco integration
    public Method getInterfaceMethod() {
        return method;
    }

    public boolean isFunction() {
        return getActualReturnType() != SVoidType.instance;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        throw new InternalException();
    }

    @Override
    public int getLoadInst() {
        return ALOAD;
    }

    @Override
    public int getStoreInst() {
        return ASTORE;
    }

    @Override
    public int getArrayLoadInst() {
        return AALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return AASTORE;
    }

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public int getReturnInst() {
        return ARETURN;
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

    public int getParameterStackIndex(int index) {
        return paramStackIndexes[index];
    }

    public String getMethodName() {
        return method.getName();
    }

    public String getMethodDescriptor() {
        return Type.getMethodDescriptor(method);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (SType type : actualParameters) {
            sb.append(type.toString()).append(", ");
        }
        if (actualParameters.length > 0) {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(") => ");
        sb.append(actualReturnType.toString());
        return sb.toString();
    }

    private static int findTypeParamIndex(TypeVariable<? extends Class<?>>[] params, String name) {
        int index = -1;
        for (int i = 0; i < params.length; i++) {
            if (params[i].getName().equals(name)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new InternalException();
        }
        return index;
    }
}