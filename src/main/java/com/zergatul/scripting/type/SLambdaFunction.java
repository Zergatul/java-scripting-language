package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.runtime.*;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

public class SLambdaFunction extends SType {

    private final SType returnType;
    private final SType[] parameters;

    public SLambdaFunction(SType returnType, SType... parameters) {
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public SType getReturnType() {
        return returnType;
    }

    public boolean isFunction() {
        return returnType != SVoidType.instance;
    }

    public SType[] getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SLambdaFunction other) {
            if (!returnType.equals(other.returnType)) {
                return false;
            }
            if (parameters.length != other.parameters.length) {
                return false;
            }
            for (int i = 0; i < parameters.length; i++) {
                if (!parameters[i].equals(other.parameters[i])) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Class<?> getJavaClass() {
        if (returnType == SVoidType.instance) {
            return switch (parameters.length) {
                case 0 -> Action0.class;
                case 1 -> Action1.class;
                case 2 -> Action2.class;
                default -> throw new InternalException("Too much Action parameters.");
            };
        } else {
            return switch (parameters.length) {
                case 0 -> Function0.class;
                case 1 -> Function1.class;
                case 2 -> Function2.class;
                default -> throw new InternalException("Too much Function parameters.");
            };
        }
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
    public String toString() {
        if (returnType == SVoidType.instance) {
            if (parameters.length == 0) {
                return "Action";
            } else {
                return "Action<" + String.join(", ", Arrays.stream(this.parameters).map(Object::toString).toArray(String[]::new)) + ">";
            }
        } else {
            return "Function<" + returnType.toString() + ">";
        }
    }
}
