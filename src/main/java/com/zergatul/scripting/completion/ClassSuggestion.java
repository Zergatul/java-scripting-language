package com.zergatul.scripting.completion;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class ClassSuggestion {

    private final String value;
    private final ClassSuggestionType type;

    public ClassSuggestion(String value, ClassSuggestionType type) {
        this.value = value;
        this.type = type;
    }

    public String value() {
        return value;
    }

    public ClassSuggestionType type() {
        return type;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ClassSuggestion that = (ClassSuggestion) obj;
        return  Objects.equals(this.value, that.value) &&
                Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    @Override
    public String toString() {
        return  "ClassSuggestion[" +
                "value=" + value + ", " +
                "type=" + type + ']';
    }
}