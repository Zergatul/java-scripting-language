package com.zergatul.scripting.tests.framework;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

public final class Difference {

    private final String path;
    private final String message;

    public Difference(String path, String message) {
        this.path = path;
        this.message = message;
    }

    public String path() {
        return path;
    }

    public String message() {
        return message;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Difference that = (Difference) obj;
        return  Objects.equals(this.path, that.path) &&
                Objects.equals(this.message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, message);
    }

    @Override
    public String toString() {
        return  "Difference[" +
                "path=" + path + ", " +
                "message=" + message + ']';
    }
}