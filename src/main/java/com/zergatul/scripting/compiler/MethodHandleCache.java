package com.zergatul.scripting.compiler;

import com.zergatul.scripting.type.SType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MethodHandleCache {

    public static final String CLASS_NAME = "MethodHandleCache";
    public static final String INTERNAL_NAME = "com/zergatul/scripting/dynamic/" + CLASS_NAME;

    private final Map<Field, String> fields = new HashMap<>();
    private final Map<Method, String> methods = new HashMap<>();

    public String createFieldAccess(Field field) {
        String handleFieldName = fields.get(field);
        if (handleFieldName != null) {
            return handleFieldName;
        }

        handleFieldName = String.format("$_%s_$_%s_$_var_handle",
                SType.fromJavaType(field.getDeclaringClass()).asMethodPart(),
                field.getName());
        handleFieldName = uniquify(fields.values(), handleFieldName);
        fields.put(field, handleFieldName);
        return handleFieldName;
    }

    public String createMethodAccess(Method method) {
        String handleFieldName = methods.get(method);
        if (handleFieldName != null) {
            return handleFieldName;
        }

        handleFieldName = String.format("$_%s_$_%s_$_%s_$_method_handle",
                SType.fromJavaType(method.getDeclaringClass()).asMethodPart(),
                method.getName(),
                method.getParameterCount());
        handleFieldName = uniquify(methods.values(), handleFieldName);
        methods.put(method, handleFieldName);
        return handleFieldName;
    }

    public Map<Field, String> getFieldsMap() {
        return fields;
    }

    public Map<Method, String> getMethodsMap() {
        return methods;
    }

    private String uniquify(Collection<String> keys, String candidate) {
        if (!keys.contains(candidate)) {
            return candidate;
        }

        int suffix = 0;
        while (keys.contains(candidate + "_" + suffix)) {
            suffix++;
        }

        return candidate + "_" + suffix;
    }
}