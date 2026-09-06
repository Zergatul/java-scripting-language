package com.zergatul.scripting.tests.completion.helpers;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class TestCompletionContext {

    private final CompilationParameters parameters;
    private final BinderOutput output;

    public TestCompletionContext(CompilationParameters parameters, BinderOutput output) {
        this.parameters = parameters;
        this.output = output;
    }

    public CompilationParameters parameters() {
        return parameters;
    }

    public BinderOutput output() {
        return output;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        TestCompletionContext that = (TestCompletionContext) obj;
        return  Objects.equals(this.parameters, that.parameters) &&
                Objects.equals(this.output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, output);
    }

    @Override
    public String toString() {
        return  "TestCompletionContext[" +
                "parameters=" + parameters + ", " +
                "output=" + output + ']';
    }
}