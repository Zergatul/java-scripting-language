package com.zergatul.scripting.compiler;

import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Method;
import java.util.Optional;

@NullMarked
public interface MethodUsagePolicy {
    Optional<String> validate(Method method);
}