package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

public class BoundParameterNode extends BoundNode {

    private final BoundNameExpressionNode name;
    private final BoundTypeNode typeNode;
    private final SType type;

    public BoundParameterNode(BoundNameExpressionNode name, BoundTypeNode typeNode, TextRange range) {
        super(NodeType.PARAMETER, range);
        this.name = name;
        this.typeNode = typeNode;
        this.type = typeNode.type;
    }

    public BoundParameterNode(BoundNameExpressionNode name, SType type, TextRange range) {
        super(NodeType.PARAMETER, range);
        this.name = name;
        this.typeNode = null;
        this.type = type;
    }

    public BoundNameExpressionNode getName() {
        return name;
    }

    public BoundTypeNode getTypeNode() {
        return typeNode;
    }

    public SType getType() {
        return type;
    }
}