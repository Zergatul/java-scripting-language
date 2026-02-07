package com.zergatul.scripting.compiler.frames;

import com.zergatul.scripting.InternalException;
import org.jspecify.annotations.Nullable;

public abstract class Frame {

    public final @Nullable Frame parent;
    public final FrameKind kind;

    protected Frame(@Nullable Frame parent, FrameKind kind) {
        this.parent = parent;
        this.kind = kind;
    }

    public @Nullable LoopFrame getClosestLoop() {
        for (Frame frame = this; frame != null; frame = frame.parent) {
            if (frame instanceof LoopFrame loop) {
                return loop;
            }
        }

        return null;
    }

    public FunctionFrame getFunction() {
        for (Frame frame = this; frame != null; frame = frame.parent) {
            if (frame instanceof FunctionFrame function) {
                return function;
            }
        }

        throw new InternalException();
    }
}