package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.BinaryExpressionNode;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SBoolean;

import java.util.List;

public class BoundInExpressionNode extends BoundExpressionNode {

    public final BinaryExpressionNode syntaxNode;
    public final BoundExpressionNode left;
    public final BoundExpressionNode right;
    public final MethodReference method;

    public BoundInExpressionNode(BinaryExpressionNode node, BoundExpressionNode left, BoundExpressionNode right, MethodReference method) {
        this(node, left, right, method, node.getRange());
    }

    public BoundInExpressionNode(BinaryExpressionNode node, BoundExpressionNode left, BoundExpressionNode right, MethodReference method, TextRange range) {
        super(BoundNodeType.IN_EXPRESSION, SBoolean.instance, range);
        this.syntaxNode = node;
        this.left = left;
        this.right = right;
        this.method = method;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        left.accept(visitor);
        right.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(left, right);
    }
}