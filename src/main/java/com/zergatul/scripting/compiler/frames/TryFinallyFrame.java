package com.zergatul.scripting.compiler.frames;

import com.zergatul.scripting.binding.nodes.BoundBlockStatementNode;

public class TryFinallyFrame extends Frame {

    public final BoundBlockStatementNode finallyBlock;

    public TryFinallyFrame(Frame parent, BoundBlockStatementNode finallyBlock) {
        super(parent, FrameKind.TRY_FINALLY);
        this.finallyBlock = finallyBlock;
    }
}