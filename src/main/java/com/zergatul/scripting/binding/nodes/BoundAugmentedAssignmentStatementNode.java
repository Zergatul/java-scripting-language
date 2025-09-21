package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;

import java.util.List;

public class BoundAugmentedAssignmentStatementNode extends BoundStatementNode {

    public final BoundExpressionNode left;
    public final BoundAssignmentOperatorNode assignmentOperator;
    public final BoundBinaryOperatorNode operator;
    public final BoundExpressionNode right;

    public BoundAugmentedAssignmentStatementNode(BoundExpressionNode left, BoundAssignmentOperatorNode assignmentOperator, BoundBinaryOperatorNode operator, BoundExpressionNode right) {
        this(left, assignmentOperator, operator, right, null);
    }

    public BoundAugmentedAssignmentStatementNode(BoundExpressionNode left, BoundAssignmentOperatorNode assignmentOperator, BoundBinaryOperatorNode operator, BoundExpressionNode right, TextRange range) {
        super(BoundNodeType.AUGMENTED_ASSIGNMENT_STATEMENT, range);
        this.left = left;
        this.assignmentOperator = assignmentOperator;
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
        assignmentOperator.accept(visitor);
        operator.accept(visitor);
        right.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(left, assignmentOperator, operator, right);
    }
}