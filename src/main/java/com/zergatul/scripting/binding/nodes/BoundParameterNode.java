package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

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

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        if (typeNode != null) {
            typeNode.accept(visitor);
        }
        name.accept(visitor);
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

    @Override
    public List<BoundNode> getChildren() {
        return typeNode == null ? List.of(name) : List.of(name, typeNode);
    }
}