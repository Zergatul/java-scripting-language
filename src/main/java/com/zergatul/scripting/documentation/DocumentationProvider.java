package com.zergatul.scripting.documentation;

import com.zergatul.scripting.type.*;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class DocumentationProvider {

    public @Nullable String getTypeDocs(SType type) {
        if (type == SBoolean.instance) {
            return "true or false value";
        }
        if (type == SInt8.instance) {
            return "8-bit signed integer";
        }
        if (type == SInt16.instance) {
            return "16-bit signed integer";
        }
        if (type == SInt.instance) {
            return "32-bit signed integer";
        }
        if (type == SInt64.instance) {
            return "64-bit signed integer";
        }
        if (type == SChar.instance) {
            return "Single character";
        }
        if (type == SFloat32.instance) {
            return "Single-precision floating-point number";
        }
        if (type == SFloat.instance) {
            return "Double-precision floating-point number";
        }
        if (type == SString.instance) {
            return "Text as sequence of characters";
        }
        return null;
    }

    public Optional<String> getMethodDocumentation(MethodReference method) {
        return method.getDescription();
    }

    public Optional<String> getPropertyDocumentation(PropertyReference property) {
        return property.getDescription();
    }

    public Optional<String> getConstructorDocumentation(ConstructorReference constructor) {
        return constructor.getDescription();
    }
}