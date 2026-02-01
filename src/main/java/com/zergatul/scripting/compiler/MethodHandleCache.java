package com.zergatul.scripting.compiler;

import com.zergatul.scripting.type.SType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class MethodHandleCache {

    public static final String CLASS_NAME = "com/zergatul/scripting/dynamic/MethodHandleCache";

    private final Map<Field, String> fields = new HashMap<>();

    public String createFieldAccess(Field field) {
        String handleFieldName = fields.get(field);
        if (handleFieldName != null) {
            return handleFieldName;
        }

        handleFieldName = String.format("$_%s_$_%s_$_var_handle",
                SType.fromJavaType(field.getDeclaringClass()).asMethodPart(),
                field.getName());
        fields.put(field, handleFieldName);
        return handleFieldName;
    }

    public Map<Field, String> getFieldsMap() {
        return fields;
    }
}