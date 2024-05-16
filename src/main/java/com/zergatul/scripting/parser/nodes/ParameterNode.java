package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class ParameterNode extends Node {

    private final TypeNode type;
    private final NameExpressionNode name;

    public ParameterNode(TypeNode type, NameExpressionNode name, TextRange range) {
        super(NodeType.PARAMETER, range);
        this.type = type;
        this.name = name;
    }

    public TypeNode getType() {
        return type;
    }

    public NameExpressionNode getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParameterNode other) {
            return other.type.equals(type) && other.name.equals(name);
        } else {
            return false;
        }
    }
}