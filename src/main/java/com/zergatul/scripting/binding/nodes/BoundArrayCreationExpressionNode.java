package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ArrayCreationExpressionNode;
import com.zergatul.scripting.type.SArrayType;

import java.util.List;

public class BoundArrayCreationExpressionNode extends BoundExpressionNode {

    public final ArrayCreationExpressionNode syntaxNode;
    public final BoundTypeNode typeNode;
    public final BoundExpressionNode lengthExpression;

    public BoundArrayCreationExpressionNode(ArrayCreationExpressionNode node, BoundTypeNode typeNode, BoundExpressionNode lengthExpression) {
        this(node, typeNode, lengthExpression, node.getRange());
    }

    public BoundArrayCreationExpressionNode(
            ArrayCreationExpressionNode node,
            BoundTypeNode typeNode,
            BoundExpressionNode lengthExpression,
            TextRange range
    ) {
        super(BoundNodeType.ARRAY_CREATION_EXPRESSION, new SArrayType(typeNode.type), range);
        this.syntaxNode = node;
        this.typeNode = typeNode;
        this.lengthExpression = lengthExpression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        typeNode.accept(visitor);
        lengthExpression.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(typeNode, lengthExpression);
    }
}