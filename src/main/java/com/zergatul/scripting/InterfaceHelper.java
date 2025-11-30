package com.zergatul.scripting;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class InterfaceHelper {

    public static boolean isFuncInterface(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return false;
        }
        List<Method> methods = Arrays.stream(clazz.getMethods())
                .filter(InterfaceHelper::isNormalInterfaceMethod)
                .toList();
        return methods.size() == 1;
    }

    public static Method getFuncInterfaceMethod(Class<?> clazz) {
        if (!clazz.isInterface()) {
            throw new InternalException();
        }
        List<Method> methods = Arrays.stream(clazz.getMethods())
                .filter(InterfaceHelper::isNormalInterfaceMethod)
                .toList();
        if (methods.size() != 1) {
            throw new InternalException();
        }
        return methods.getFirst();
    }

    private static boolean isNormalInterfaceMethod(Method method) {
        return !method.isDefault() && !Modifier.isStatic(method.getModifiers());
    }
}