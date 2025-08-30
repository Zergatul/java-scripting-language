package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.Type;

public class SGenericFunction extends SFunction {

    private String internalName;
    private Class<?> clazz;

    public SGenericFunction(SType returnType, SType[] parameters) {
        super(returnType, createParameters(parameters));
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public void setJavaClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    public String getShortClassName() {
        String[] parts = internalName.split("/");
        return parts[parts.length - 1];
    }

    public String getMethodName() {
        return "apply";
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public String getDescriptor() {
        if (internalName == null) {
            throw new InternalException();
        } else {
            return Type.getObjectType(internalName).getDescriptor();
        }
    }

    @Override
    public Class<?> getJavaClass() {
        return clazz;
    }

    private static MethodParameter[] createParameters(SType[] types) {
        MethodParameter[] parameters = new MethodParameter[types.length];
        for (int i = 0; i < types.length; i++) {
            parameters[i] = new MethodParameter("p" + i, types[i]);
        }
        return parameters;
    }
}