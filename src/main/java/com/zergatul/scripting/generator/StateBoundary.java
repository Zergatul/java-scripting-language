package com.zergatul.scripting.generator;

import com.zergatul.scripting.binding.nodes.BoundStatementNode;
import org.objectweb.asm.Label;

import java.util.ArrayList;
import java.util.List;

public class StateBoundary {

    public final int index;
    public final Label label;
    public final List<BoundStatementNode> statements;

    public StateBoundary(int index) {
        this.index = index;
        this.label = new Label();
        this.statements = new ArrayList<>();
    }
}