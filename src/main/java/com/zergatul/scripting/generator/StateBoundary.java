package com.zergatul.scripting.generator;

import com.zergatul.scripting.binding.nodes.BoundStatementNode;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Label;

import java.util.ArrayList;
import java.util.List;

public class StateBoundary {

    public final Label label;
    public final List<BoundStatementNode> statements;
    public final @Nullable StateBoundary catchState;

    public StateBoundary(@Nullable StateBoundary catchState) {
        this.label = new Label();
        this.statements = new ArrayList<>();
        this.catchState = catchState;
    }
}