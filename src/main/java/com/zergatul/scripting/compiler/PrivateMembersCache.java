package com.zergatul.scripting.compiler;

import com.zergatul.scripting.type.SType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PrivateMembersCache {

    public static final String CLASS_NAME = "PrivateMembersCache";
    public static final String INTERNAL_NAME = "com/zergatul/scripting/dynamic/" + CLASS_NAME;

    private final Map<Field, String> fields = new HashMap<>();
    private final Map<Method, String> methods = new HashMap<>();

    public String createFieldAccess(Field field) {
        String fieldName = fields.get(field);
        if (fieldName != null) {
            return fieldName;
        }

        fieldName = String.format("$_%s_$_%s_$_field",
                SType.fromJavaType(field.getDeclaringClass()).asMethodPart(),
                field.getName());
        fieldName = uniquify(fields.values(), fieldName);
        fields.put(field, fieldName);
        return fieldName;
    }

    public String createMethodAccess(Method method) {
        String fieldName = methods.get(method);
        if (fieldName != null) {
            return fieldName;
        }

        fieldName = String.format("$_%s_$_%s_$_%s_$_method",
                SType.fromJavaType(method.getDeclaringClass()).asMethodPart(),
                method.getName(),
                method.getParameterCount());
        fieldName = uniquify(methods.values(), fieldName);
        methods.put(method, fieldName);
        return fieldName;
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