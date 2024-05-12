package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.MethodReference;

import java.util.List;

public class BoundMethodNode extends BoundNode {

    public final MethodReference method;

    public BoundMethodNode(MethodReference method, TextRange range) {
        super(NodeType.METHOD, range);
        this.method = method;
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}