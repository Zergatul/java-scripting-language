package com.zergatul.scripting.type;

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