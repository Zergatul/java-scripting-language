package com.zergatul.scripting.generator;

import com.zergatul.scripting.binding.nodes.BoundStatementNode;
import org.objectweb.asm.Label;

import java.util.ArrayList;
import java.util.List;

public class StateBoundary {

    public int index;
    public final Label label;
    public final List<BoundStatementNode> statements;

    public StateBoundary() {
        this(-1);
    }

    public StateBoundary(int index) {
        this.index = index;
        this.label = new Label();
        this.statements = new ArrayList<>();
    }
}