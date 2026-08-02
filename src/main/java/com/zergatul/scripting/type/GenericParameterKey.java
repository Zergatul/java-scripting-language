package com.zergatul.scripting.type;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;

public record GenericParameterKey(GenericDeclaration declaration, int index, String name) {

    public static GenericParameterKey from(TypeVariable<?> parameter) {
        var pp = (TypeVariable<GenericDeclaration>) parameter;
        return new GenericParameterKey(parameter.getGenericDeclaration(), 0, pp.getName());
    }
}