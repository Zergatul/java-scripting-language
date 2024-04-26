package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundParameterListNode extends BoundNode {

    public final List<BoundParameterNode> parameters;

    public BoundParameterListNode(List<BoundParameterNode> parameters, TextRange range) {
        super(NodeType.PARAMETER_LIST, range);
        this.parameters = parameters;
    }
}