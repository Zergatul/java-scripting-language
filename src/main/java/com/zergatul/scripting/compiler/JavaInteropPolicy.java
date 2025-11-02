package com.zergatul.scripting.compiler;

import java.lang.reflect.Method;

public abstract class JavaInteropPolicy {
    public abstract boolean isMethodVisible(Method method);
    public abstract boolean isJavaTypeUsageAllowed();
    public abstract String getJavaTypeUsageError();
    public abstract ClassLoader getClassLoader();
}