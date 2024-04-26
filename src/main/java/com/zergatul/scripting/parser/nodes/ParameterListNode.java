package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;
import java.util.Objects;

public class ParameterListNode extends Node {

    public final List<ParameterNode> parameters;

    public ParameterListNode(List<ParameterNode> parameters, TextRange range) {
        super(NodeType.PARAMETER_LIST, range);
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParameterListNode other) {
            return Objects.equals(other.parameters, parameters) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}