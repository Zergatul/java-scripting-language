package com.zergatul.scripting.compiler.frames;

import com.zergatul.scripting.symbols.LocalVariable;

public class TryCatchFrame extends Frame {

    public final LocalVariable exceptionVariable;

    public TryCatchFrame(Frame parent, LocalVariable exceptionVariable) {
        super(parent, FrameKind.TRY_CATCH);
        this.exceptionVariable = exceptionVariable;
    }
}