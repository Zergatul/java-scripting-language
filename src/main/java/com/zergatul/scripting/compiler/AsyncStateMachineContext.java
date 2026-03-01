package com.zergatul.scripting.compiler;

import com.zergatul.scripting.generator.StateBoundary;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Label;

import java.util.Map;
import java.util.Objects;

public class AsyncStateMachineContext {

    private final String className;
    private final Map<StateBoundary, Integer> statesMap;
    private final Label continueLabel;
    private final boolean hasPendingReturn;
    private final boolean hasPendingJump;

    private AsyncStateMachineContext(
            String className,
            Map<StateBoundary, Integer> statesMap,
            Label continueLabel,
            boolean hasPendingReturn,
            boolean hasPendingJump
    ) {
        this.className = className;
        this.statesMap = statesMap;
        this.continueLabel = continueLabel;
        this.hasPendingReturn = hasPendingReturn;
        this.hasPendingJump = hasPendingJump;
    }

    public String getClassName() {
        return className;
    }

    public int getStateIndex(StateBoundary boundary) {
        return statesMap.get(boundary);
    }

    public Label getContinueLabel() {
        return continueLabel;
    }

    public boolean hasPendingReturn() {
        return hasPendingReturn;
    }

    public boolean hasPendingJump() {
        return hasPendingJump;
    }

    public static class Builder {

        private @Nullable String className;
        private @Nullable Map<StateBoundary, Integer> statesMap;
        private @Nullable Label continueLabel;
        private boolean hasPendingReturn;
        private boolean hasPendingJump;

        public Builder setClassName(String className) {
            this.className = className;
            return this;
        }

        public Builder setStatesMap(Map<StateBoundary, Integer> map) {
            this.statesMap = map;
            return this;
        }

        public Builder setContinueLabel(Label label) {
            this.continueLabel = label;
            return this;
        }

        public Builder setPendingReturn(boolean value) {
            this.hasPendingReturn = value;
            return this;
        }

        public Builder setPendingJump(boolean value) {
            this.hasPendingJump = value;
            return this;
        }

        public AsyncStateMachineContext build() {
            return new AsyncStateMachineContext(
                    Objects.requireNonNull(className),
                    Objects.requireNonNull(statesMap),
                    Objects.requireNonNull(continueLabel),
                    hasPendingReturn,
                    hasPendingJump);
        }
    }
}