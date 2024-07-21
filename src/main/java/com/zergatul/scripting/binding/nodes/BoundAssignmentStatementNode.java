package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundAssignmentStatementNode extends BoundStatementNode {

    public final BoundExpressionNode left;
    public final BoundAssignmentOperatorNode operator;
    public final BoundExpressionNode right;

    public BoundAssignmentStatementNode(BoundExpressionNode left, BoundAssignmentOperatorNode operator, BoundExpressionNode right) {
        this(left, operator, right, null);
    }

    public BoundAssignmentStatementNode(BoundExpressionNode left, BoundAssignmentOperatorNode operator, BoundExpressionNode right, TextRange range) {
        super(NodeType.ASSIGNMENT_STATEMENT, range);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        left.accept(visitor);
        operator.accept(visitor);
        right.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(left, operator, right);
    }
}