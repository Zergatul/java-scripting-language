package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BoundNewExpressionNode extends BoundExpressionNode {

    public final BoundTypeNode typeNode;
    public final BoundExpressionNode lengthExpression;
    public final List<BoundExpressionNode> items;

    public BoundNewExpressionNode(BoundTypeNode typeNode, BoundExpressionNode lengthExpression, List<BoundExpressionNode> items, TextRange range) {
        super(NodeType.NEW_EXPRESSION, typeNode.type, range);
        this.typeNode = typeNode;
        this.lengthExpression = lengthExpression;
        this.items = items;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        typeNode.accept(visitor);
        if (lengthExpression != null) {
            lengthExpression.accept(visitor);
        }
        if (items != null) {
            for (BoundExpressionNode expression : items) {
                expression.accept(visitor);
            }
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        List<BoundNode> children = new ArrayList<>();
        children.add(typeNode);
        if (lengthExpression != null) {
            children.add(lengthExpression);
        }
        if (items != null) {
            children.addAll(items);
        }
        return children;
    }
}