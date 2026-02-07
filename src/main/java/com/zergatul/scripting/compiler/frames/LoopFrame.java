package com.zergatul.scripting.compiler.frames;

import org.objectweb.asm.Label;

public class LoopFrame extends Frame {

    public final Label breakLabel;
    public final Label continueLabel;

    public LoopFrame(Frame parent, Label breakLabel, Label continueLabel) {
        super(parent, FrameKind.LOOP);
        this.breakLabel = breakLabel;
        this.continueLabel = continueLabel;
    }
}