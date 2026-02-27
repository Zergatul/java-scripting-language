package com.zergatul.scripting.generator;

import com.zergatul.scripting.binding.nodes.BoundStatementNode;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Label;

import java.util.ArrayList;
import java.util.List;

public class StateBoundary {

    public final boolean isMainCatch;
    public final Label label;
    public final List<BoundStatementNode> statements;
    public final @Nullable StateBoundary catchState;

    public StateBoundary(boolean isMainCatch) {
        this.isMainCatch = true;
        this.label = new Label();
        this.statements = new ArrayList<>();
        this.catchState = null;
    }

    public StateBoundary(@Nullable StateBoundary catchState) {
        this.isMainCatch = false;
        this.label = new Label();
        this.statements = new ArrayList<>();
        this.catchState = catchState;
    }
}