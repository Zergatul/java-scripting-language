package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundArrayCreationExpressionNode extends BoundExpressionNode {

    public final BoundTypeNode typeNode;
    public final BoundExpressionNode lengthExpression;

    public BoundArrayCreationExpressionNode(BoundTypeNode typeNode, BoundExpressionNode lengthExpression, TextRange range) {
        super(NodeType.ARRAY_CREATION_EXPRESSION, typeNode.type, range);
        this.typeNode = typeNode;
        this.lengthExpression = lengthExpression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public BoundNode accept(BinderTreeRewriter rewriter) {
        return rewriter.visit(this);
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