package com.zergatul.scripting.type;

import java.util.List;

public interface Invocable {

    List<MethodParameter> getParameters();

    default List<SType> getParameterTypes() {
        return getParameters().stream().map(MethodParameter::type).toList();
    }
}