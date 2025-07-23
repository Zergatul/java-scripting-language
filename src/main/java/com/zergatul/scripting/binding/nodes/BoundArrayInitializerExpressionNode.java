package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.ArrayList;
import java.util.List;

public class BoundArrayInitializerExpressionNode extends BoundExpressionNode {

    public final BoundTypeNode typeNode;
    public final List<BoundExpressionNode> items;

    public BoundArrayInitializerExpressionNode(BoundTypeNode typeNode, List<BoundExpressionNode> items, TextRange range) {
        super(NodeType.ARRAY_INITIALIZER_EXPRESSION, typeNode.type, range);
        this.typeNode = typeNode;
        this.items = items;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        typeNode.accept(visitor);
        for (BoundExpressionNode expression : items) {
            expression.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        List<BoundNode> list = new ArrayList<>();
        list.add(typeNode);
        list.addAll(items);
        return list;
    }
}