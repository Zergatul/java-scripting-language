package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class StaticVariableDeclaration {

    private final String name;
    private final BoundTypeNode typeNode;
    private final boolean hasError;

    public StaticVariableDeclaration(String name, BoundTypeNode typeNode, boolean hasError) {
        this.name = name;
        this.typeNode = typeNode;
        this.hasError = hasError;
    }

    public String name() {
        return name;
    }

    public BoundTypeNode typeNode() {
        return typeNode;
    }

    public boolean hasError() {
        return hasError;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        StaticVariableDeclaration that = (StaticVariableDeclaration) obj;
        return  Objects.equals(this.name, that.name) &&
                Objects.equals(this.typeNode, that.typeNode) &&
                this.hasError == that.hasError;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, typeNode, hasError);
    }

    @Override
    public String toString() {
        return  "StaticVariableDeclaration[" +
                "name=" + name + ", " +
                "typeNode=" + typeNode + ", " +
                "hasError=" + hasError + ']';
    }
}