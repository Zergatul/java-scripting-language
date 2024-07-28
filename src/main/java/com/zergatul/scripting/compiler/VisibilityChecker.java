package com.zergatul.scripting.compiler;

import java.lang.reflect.Method;

public abstract class VisibilityChecker {
    public abstract boolean isVisible(Method method);
}